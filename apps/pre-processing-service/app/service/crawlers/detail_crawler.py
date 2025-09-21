import time
import re
from bs4 import BeautifulSoup
from .search_crawler import SearchCrawler
from loguru import logger


class DetailCrawler(SearchCrawler):
    """SearchCrawler를 확장한 상세 크롤링 클래스"""

    async def crawl_detail(self, product_url: str) -> dict:
        """상품 상세 정보 크롤링 (이미지 항상 포함)"""
        try:
            logger.info(f"상품 상세 크롤링 시작: url='{product_url}'")

            # HTML 가져오기
            soup = (
                await self._get_soup_selenium(product_url)
                if self.use_selenium
                else await self._get_soup_httpx(product_url)
            )

            # 기본 정보 추출
            title = self._extract_title(soup)
            price = self._extract_price(soup)
            rating = self._extract_rating(soup)
            options = self._extract_options(soup)
            material_info = self._extract_material_info(soup)

            # 이미지 정보 추출 (항상 실행)
            logger.info("이미지 정보 추출 중...")
            page_images = self._extract_images(soup)
            option_images = [
                opt["image_url"] for opt in options if opt.get("image_url")
            ]
            # 중복 제거 후 합치기
            all_images = list(set(page_images + option_images))

            product_data = {
                "url": product_url,
                "title": title,
                "price": price,
                "rating": rating,
                "options": options,
                "material_info": material_info,
                "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
                "product_images": [{"original_url": url} for url in all_images],
            }

            logger.info(f"추출된 이미지: {len(all_images)}개")
            logger.info(
                f"상품 상세 크롤링 완료: title='{title[:50]}', price={price}, rating={rating}, options_count={len(options)}"
            )
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
        title_element = soup.find("h1", {"id": "kakaotitle"})
        title = title_element.get_text(strip=True) if title_element else "제목 없음"
        logger.debug(f"제목 추출: '{title[:50]}'")
        return title

    def _extract_price(self, soup: BeautifulSoup) -> int:
        price = 0
        selectors = [
            "span.price.gsItemPriceKWR",
            ".pdt_price span.price",
            "span.price",
            ".price",
        ]
        for sel in selectors:
            el = soup.select_one(sel)
            if el:
                text = el.get_text(strip=True).replace(",", "").replace("원", "")
                match = re.search(r"(\d+)", text)
                if match:
                    price = int(match.group(1))
                    logger.debug(f"가격 추출 성공: {price}원 (selector: {sel})")
                    break
        if price == 0:
            logger.debug("가격 추출 실패 - 0원으로 설정")
        return price

    def _extract_rating(self, soup: BeautifulSoup) -> float:
        rating = 0.0
        containers = [
            soup.find("a", class_="start"),
            soup.find("div", class_=re.compile(r"star|rating")),
            soup.find("a", href="#reviews_wrap"),
        ]
        for cont in containers:
            if cont:
                imgs = cont.find_all("img")
                for img in imgs:
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
        options = []
        sku_list = soup.find("ul", {"id": "skubox"})
        if sku_list:
            items = sku_list.find_all("li", class_=re.compile(r"imgWrapper"))
            for item in items:
                title_el = item.find("a", title=True)
                if title_el:
                    name = title_el.get("title", "").strip()
                    stock = 0
                    stock_match = re.search(r"재고\s*:\s*(\d+)", item.get_text())
                    if stock_match:
                        stock = int(stock_match.group(1))
                    img_el = item.find("img", class_="colorSpec_hashPic")
                    img_url = img_el["src"] if img_el and img_el.get("src") else ""
                    if name:
                        options.append(
                            {"name": name, "stock": stock, "image_url": img_url}
                        )
        logger.info(f"총 {len(options)}개 옵션 추출 완료")
        return options

    def _extract_material_info(self, soup: BeautifulSoup) -> dict:
        material_info = {}
        items = soup.find_all("div", class_="pro-info-item")
        for item in items:
            title_el = item.find("div", class_="pro-info-title")
            info_el = item.find("div", class_="pro-info-info")
            if title_el and info_el:
                material_info[title_el.get_text(strip=True)] = info_el.get_text(
                    strip=True
                )
        logger.info(f"총 {len(material_info)}개 소재 정보 추출 완료")
        return material_info

    def _extract_images(self, soup: BeautifulSoup) -> list[str]:
        images = []
        # img_translate_x 패턴
        img_elements = soup.find_all("img", {"id": re.compile(r"img_translate_\d+")})
        for img in img_elements:
            src = img.get("src") or img.get("data-src")
            if not src:
                continue
            if src.startswith("//"):
                src = "https:" + src
            elif src.startswith("/"):
                src = self.base_url + src
            images.append(src)
        logger.info(f"총 {len(images)}개 이미지 URL 추출 완료")
        return images