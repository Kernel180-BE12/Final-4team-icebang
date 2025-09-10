import urllib.parse
import httpx
import re
import time
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from loguru import logger


class SearchCrawler:
    def __init__(self, use_selenium=True):
        self.base_url = "https://ssadagu.kr"
        self.use_selenium = use_selenium

        if use_selenium:
            self._setup_selenium()
        else:
            self._setup_httpx()

    def _setup_selenium(self):
        """Selenium WebDriver 초기화"""
        chrome_options = Options()
        chrome_options.add_argument("--headless")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--window-size=1920,1080")
        chrome_options.add_argument(
            "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )

        try:
            self.driver = webdriver.Chrome(options=chrome_options)
            self.wait = WebDriverWait(self.driver, 10)
            logger.info("Selenium WebDriver 초기화 완료")
        except Exception as e:
            logger.warning(f"Selenium 초기화 실패, httpx로 대체: {e}")
            self.use_selenium = False
            self._setup_httpx()

    def _setup_httpx(self):
        """httpx 클라이언트 초기화"""
        self.client = httpx.AsyncClient(
            headers={
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            },
            timeout=30.0,
        )
        logger.info("httpx 클라이언트 초기화 완료")

    async def search_products_selenium(self, keyword: str) -> list[dict]:
        """Selenium을 사용한 상품 검색"""
        encoded_keyword = urllib.parse.quote(keyword)
        search_url = f"{self.base_url}/shop/search.php?ss_tx={encoded_keyword}"

        try:
            logger.info(
                f"Selenium 상품 검색 시작: keyword='{keyword}', url='{search_url}'"
            )
            self.driver.get(search_url)
            time.sleep(5)

            product_links = []
            link_elements = self.driver.find_elements(By.TAG_NAME, "a")

            for element in link_elements:
                href = element.get_attribute("href")
                if (
                    href
                    and "view.php" in href
                    and ("platform=1688" in href or "num_iid" in href)
                ):
                    try:
                        title = element.get_attribute("title") or element.text.strip()
                        if title:
                            product_links.append({"url": href, "title": title})
                    except:
                        product_links.append({"url": href, "title": "Unknown Title"})

            # 중복 제거
            seen_urls = set()
            unique_products = []
            for product in product_links:
                if product["url"] not in seen_urls:
                    seen_urls.add(product["url"])
                    unique_products.append(product)

            logger.info(
                f"Selenium으로 발견한 상품 링크: {len(unique_products)}개 (중복 제거 전: {len(product_links)}개)"
            )
            return unique_products[:20]

        except Exception as e:
            logger.error(f"Selenium 검색 오류: keyword='{keyword}', error='{e}'")
            return []

    async def search_products_httpx(self, keyword: str) -> list[dict]:
        """httpx를 사용한 상품 검색"""
        encoded_keyword = urllib.parse.quote(keyword)
        search_url = f"{self.base_url}/shop/search.php?ss_tx={encoded_keyword}"

        try:
            logger.info(
                f"httpx 상품 검색 시작: keyword='{keyword}', url='{search_url}'"
            )
            response = await self.client.get(search_url)
            response.raise_for_status()
            soup = BeautifulSoup(response.content, "html.parser")

            product_links = []
            all_links = soup.find_all("a", href=True)

            for link in all_links:
                href = link["href"]
                if "view.php" in href and (
                    "platform=1688" in href or "num_iid" in href
                ):
                    full_url = (
                        f"{self.base_url}{href}" if href.startswith("/") else href
                    )
                    title = (
                        link.get("title", "")
                        or link.get_text(strip=True)
                        or "Unknown Title"
                    )

                    product_links.append({"url": full_url, "title": title})

            logger.info(f"httpx로 발견한 상품 링크: {len(product_links)}개")
            return product_links[:20]

        except Exception as e:
            logger.error(f"httpx 검색 오류: keyword='{keyword}', error='{e}'")
            return []

    async def get_basic_product_info(self, product_url: str) -> dict:
        """기본 상품 정보만 크롤링"""
        try:
            logger.debug(f"기본 상품 정보 크롤링 시작: url='{product_url}'")

            if self.use_selenium:
                self.driver.get(product_url)
                self.wait.until(
                    lambda driver: driver.execute_script("return document.readyState")
                    == "complete"
                )
                soup = BeautifulSoup(self.driver.page_source, "html.parser")
            else:
                response = await self.client.get(product_url)
                response.raise_for_status()
                soup = BeautifulSoup(response.content, "html.parser")

            title_element = soup.find("h1", {"id": "kakaotitle"})
            title = title_element.get_text(strip=True) if title_element else "제목 없음"

            logger.debug(f"기본 상품 정보 크롤링 완료: title='{title[:50]}'")
            return {"url": product_url, "title": title}

        except Exception as e:
            logger.error(f"기본 상품 크롤링 오류: url='{product_url}', error='{e}'")
            return None

    async def close(self):
        """리소스 정리"""
        if self.use_selenium and hasattr(self, "driver"):
            try:
                self.driver.quit()
                logger.info("Selenium WebDriver 종료 완료")
            except Exception as e:
                logger.warning(f"Selenium WebDriver 종료 중 오류: {e}")
        elif hasattr(self, "client"):
            try:
                await self.client.aclose()
                logger.info("httpx 클라이언트 종료 완료")
            except Exception as e:
                logger.warning(f"httpx 클라이언트 종료 중 오류: {e}")


class DetailCrawler(SearchCrawler):
    """SearchCrawler를 확장한 상세 크롤링 클래스"""

    async def crawl_detail(
        self, product_url: str, include_images: bool = False
    ) -> dict:
        """상품 상세 정보 크롤링"""
        try:
            logger.info(
                f"상품 상세 크롤링 시작: url='{product_url}', include_images={include_images}"
            )

            if self.use_selenium:
                soup = await self._get_soup_selenium(product_url)
            else:
                soup = await self._get_soup_httpx(product_url)

            # 기본 정보 추출
            title = self._extract_title(soup)
            price = self._extract_price(soup)
            rating = self._extract_rating(soup)
            options = self._extract_options(soup)
            material_info = self._extract_material_info(soup)

            product_data = {
                "url": product_url,
                "title": title,
                "price": price,
                "rating": rating,
                "options": options,
                "material_info": material_info,
                "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
            }

            logger.info(
                f"기본 상품 정보 추출 완료: title='{title[:50]}', price={price}, rating={rating}, options_count={len(options)}"
            )

            if include_images:
                logger.info("이미지 정보 추출 중...")
                product_images = self._extract_images(soup)
                product_data["product_images"] = [
                    {"original_url": img_url} for img_url in product_images
                ]
                logger.info(f"추출된 이미지: {len(product_images)}개")
            else:
                product_data["product_images"] = []

            logger.info(f"상품 상세 크롤링 완료: url='{product_url}'")
            return product_data

        except Exception as e:
            logger.error(f"상품 상세 크롤링 오류: url='{product_url}', error='{e}'")
            raise Exception(f"크롤링 실패: {str(e)}")

    async def _get_soup_selenium(self, product_url: str) -> BeautifulSoup:
        """Selenium으로 HTML 가져오기"""
        try:
            logger.debug(f"Selenium HTML 로딩 시작: url='{product_url}'")
            self.driver.get(product_url)
            self.wait.until(
                lambda driver: driver.execute_script("return document.readyState")
                == "complete"
            )
            time.sleep(2)
            logger.debug("Selenium HTML 로딩 완료")
            return BeautifulSoup(self.driver.page_source, "html.parser")
        except Exception as e:
            logger.error(f"Selenium HTML 로딩 실패: url='{product_url}', error='{e}'")
            raise Exception(f"Selenium HTML 로딩 실패: {e}")

    async def _get_soup_httpx(self, product_url: str) -> BeautifulSoup:
        """httpx로 HTML 가져오기"""
        try:
            logger.debug(f"httpx HTML 요청 시작: url='{product_url}'")
            response = await self.client.get(product_url)
            response.raise_for_status()
            logger.debug("httpx HTML 요청 완료")
            return BeautifulSoup(response.content, "html.parser")
        except Exception as e:
            logger.error(f"httpx HTML 요청 실패: url='{product_url}', error='{e}'")
            raise Exception(f"HTTP 요청 실패: {e}")

    def _extract_title(self, soup: BeautifulSoup) -> str:
        """제목 추출"""
        title_element = soup.find("h1", {"id": "kakaotitle"})
        title = title_element.get_text(strip=True) if title_element else "제목 없음"
        logger.debug(f"제목 추출: '{title[:50]}'")
        return title

    def _extract_price(self, soup: BeautifulSoup) -> int:
        """가격 추출"""
        price = 0
        price_selectors = [
            "span.price.gsItemPriceKWR",
            ".pdt_price span.price",
            "span.price",
            ".price",
        ]

        for selector in price_selectors:
            price_element = soup.select_one(selector)
            if price_element:
                price_text = (
                    price_element.get_text(strip=True)
                    .replace(",", "")
                    .replace("원", "")
                )
                price_match = re.search(r"(\d+)", price_text)
                if price_match:
                    price = int(price_match.group(1))
                    logger.debug(f"가격 추출 성공: {price}원 (selector: {selector})")
                    break

        if price == 0:
            logger.debug("가격 추출 실패 - 0원으로 설정")

        return price

    def _extract_rating(self, soup: BeautifulSoup) -> float:
        """평점 추출"""
        rating = 0.0
        star_containers = [
            soup.find("a", class_="start"),
            soup.find("div", class_=re.compile(r"star|rating")),
            soup.find("a", href="#reviews_wrap"),
        ]

        for container in star_containers:
            if container:
                star_imgs = container.find_all("img")
                for img in star_imgs:
                    src = img.get("src", "")
                    if "icon_star.svg" in src:
                        rating += 1
                    elif "icon_star_half.svg" in src:
                        rating += 0.5
                if rating > 0:
                    logger.debug(f"평점 추출 성공: {rating}점")
                    break

        if rating == 0.0:
            logger.debug("평점 추출 실패 - 0.0점으로 설정")

        return rating

    def _extract_options(self, soup: BeautifulSoup) -> list[dict]:
        """상품 옵션 추출"""
        options = []
        sku_list = soup.find("ul", {"id": "skubox"})

        if sku_list:
            option_items = sku_list.find_all("li", class_=re.compile(r"imgWrapper"))
            logger.debug(f"옵션 항목 발견: {len(option_items)}개")

            for item in option_items:
                title_element = item.find("a", title=True)
                if title_element:
                    option_name = title_element.get("title", "").strip()

                    # 재고 정보 추출
                    stock = 0
                    item_text = item.get_text()
                    stock_match = re.search(r"재고\s*:\s*(\d+)", item_text)
                    if stock_match:
                        stock = int(stock_match.group(1))

                    # 이미지 URL 추출
                    img_element = item.find("img", class_="colorSpec_hashPic")
                    image_url = ""
                    if img_element and img_element.get("src"):
                        image_url = img_element["src"]

                    if option_name:
                        options.append(
                            {
                                "name": option_name,
                                "stock": stock,
                                "image_url": image_url,
                            }
                        )
                        logger.debug(f"옵션 추출: name='{option_name}', stock={stock}")

        logger.info(f"총 {len(options)}개 옵션 추출 완료")
        return options

    def _extract_material_info(self, soup: BeautifulSoup) -> dict:
        """소재 정보 추출"""
        material_info = {}
        info_items = soup.find_all("div", class_="pro-info-item")

        for item in info_items:
            title_element = item.find("div", class_="pro-info-title")
            info_element = item.find("div", class_="pro-info-info")

            if title_element and info_element:
                title = title_element.get_text(strip=True)
                info = info_element.get_text(strip=True)
                material_info[title] = info
                logger.debug(f"소재 정보 추출: {title}='{info}'")

        logger.info(f"총 {len(material_info)}개 소재 정보 추출 완료")
        return material_info

    def _extract_images(self, soup: BeautifulSoup) -> list[str]:
        """상품 이미지 추출"""
        images = []
        img_elements = soup.find_all("img", {"id": re.compile(r"img_translate_\d+")})

        for img in img_elements:
            src = img.get("src", "")
            if src:
                if src.startswith("//"):
                    src = "https:" + src
                elif src.startswith("/"):
                    src = self.base_url + src
                elif src.startswith("http"):
                    pass
                else:
                    continue
                images.append(src)
                logger.debug(f"이미지 URL 추출: {src}")

        logger.info(f"총 {len(images)}개 이미지 URL 추출 완료")
        return images
