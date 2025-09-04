import json
import time
import re
import httpx
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.common.exceptions import TimeoutException, NoSuchElementException

from app.errors.CustomException import InvalidItemDataException
from app.model.schemas import RequestSadaguCrawl

async def crawl_product_detail(request: RequestSadaguCrawl) -> dict:
    """
    선택된 상품의 상세 정보를 크롤링하는 비즈니스 로직입니다.
    상품 URL을 입력받아 상세 정보를 크롤링하여 딕셔너리로 반환합니다.
    """
    crawler = ProductDetailCrawler(use_selenium=request.use_selenium)

    try:
        # 상세 정보 크롤링 실행
        product_detail = await crawler.crawl_detail(
            product_url=str(request.product_url),  # HttpUrl을 문자열로 변환
            include_images=request.include_images
        )

        if not product_detail:
            raise InvalidItemDataException("상품 상세 정보 크롤링 실패")

        # 응답 데이터 구성
        response_data = {
            "job_id": request.job_id,
            "schedule_id": request.schedule_id,
            "tag": request.tag,
            "product_url": str(request.product_url),
            "use_selenium": request.use_selenium,
            "include_images": request.include_images,
            "product_detail": product_detail,
            "status": "success",  # "200"에서 "success"로 변경
            "crawled_at": time.strftime('%Y-%m-%d %H:%M:%S')
        }

        return response_data

    except Exception as e:
        raise InvalidItemDataException(f"상품 상세 크롤링 오류: {e}")
    finally:
        await crawler.close()


class ProductDetailCrawler:
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
        chrome_options.add_argument('--headless')
        chrome_options.add_argument('--no-sandbox')
        chrome_options.add_argument('--disable-dev-shm-usage')
        chrome_options.add_argument('--disable-gpu')
        chrome_options.add_argument('--window-size=1920,1080')
        chrome_options.add_argument(
            '--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36')

        try:
            self.driver = webdriver.Chrome(options=chrome_options)
            self.wait = WebDriverWait(self.driver, 10)
        except Exception as e:
            print(f"Selenium 초기화 실패, httpx로 대체: {e}")
            self.use_selenium = False
            self._setup_httpx()

    def _setup_httpx(self):
        """httpx 클라이언트 초기화"""
        self.client = httpx.AsyncClient(
            headers={
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
            },
            timeout=30.0
        )

    async def crawl_detail(self, product_url: str, include_images: bool = False) -> dict:
        """상품 상세 정보 크롤링"""
        try:
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
                'url': product_url,
                'title': title,
                'price': price,
                'rating': rating,
                'options': options,
                'material_info': material_info,
                'crawled_at': time.strftime('%Y-%m-%d %H:%M:%S')
            }

            # 이미지 정보 추가 (선택적)
            if include_images:
                product_images = self._extract_images(soup)
                product_data['product_images'] = [{'original_url': img_url} for img_url in product_images]
            else:
                product_data['product_images'] = []

            return product_data

        except Exception as e:
            print(f"크롤링 오류: {e}")
            raise InvalidItemDataException(f"크롤링 실패: {str(e)}")

    async def _get_soup_selenium(self, product_url: str) -> BeautifulSoup:
        """Selenium으로 HTML 가져오기"""
        try:
            self.driver.get(product_url)
            self.wait.until(lambda driver: driver.execute_script("return document.readyState") == "complete")
            return BeautifulSoup(self.driver.page_source, 'html.parser')
        except Exception as e:
            raise Exception(f"Selenium HTML 로딩 실패: {e}")

    async def _get_soup_httpx(self, product_url: str) -> BeautifulSoup:
        """httpx로 HTML 가져오기"""
        try:
            response = await self.client.get(product_url)
            response.raise_for_status()
            return BeautifulSoup(response.content, 'html.parser')
        except Exception as e:
            raise Exception(f"HTTP 요청 실패: {e}")

    def _extract_title(self, soup: BeautifulSoup) -> str:
        """제목 추출"""
        title_element = soup.find('h1', {'id': 'kakaotitle'})
        return title_element.get_text(strip=True) if title_element else "제목 없음"

    def _extract_price(self, soup: BeautifulSoup) -> int:
        """가격 추출"""
        price_selectors = [
            'span.price.gsItemPriceKWR',
            '.pdt_price span.price',
            'span.price',
            '.price'
        ]

        for selector in price_selectors:
            price_element = soup.select_one(selector)
            if price_element:
                price_text = price_element.get_text(strip=True).replace(',', '').replace('원', '')
                price_match = re.search(r'(\d+)', price_text)
                if price_match:
                    return int(price_match.group(1))
        return 0

    def _extract_rating(self, soup: BeautifulSoup) -> float:
        """별점 추출"""
        rating = 0.0
        star_containers = [
            soup.find('a', class_='start'),
            soup.find('div', class_=re.compile(r'star|rating')),
            soup.find('a', href='#reviews_wrap')
        ]

        for container in star_containers:
            if container:
                star_imgs = container.find_all('img')
                for img in star_imgs:
                    src = img.get('src', '')
                    if 'icon_star.svg' in src:
                        rating += 1
                    elif 'icon_star_half.svg' in src:
                        rating += 0.5
                break
        return rating

    def _extract_options(self, soup: BeautifulSoup) -> list[dict]:
        """상품 옵션 추출"""
        options = []
        sku_list = soup.find('ul', {'id': 'skubox'})

        if sku_list:
            option_items = sku_list.find_all('li', class_=re.compile(r'imgWrapper'))
            for item in option_items:
                title_element = item.find('a', title=True)
                if title_element:
                    option_name = title_element.get('title', '').strip()

                    # 재고 정보 추출
                    stock = 0
                    item_text = item.get_text()
                    stock_match = re.search(r'재고\s*:\s*(\d+)', item_text)
                    if stock_match:
                        stock = int(stock_match.group(1))

                    # 이미지 URL 추출
                    img_element = item.find('img', class_='colorSpec_hashPic')
                    image_url = ""
                    if img_element and img_element.get('src'):
                        image_url = img_element['src']

                    if option_name:
                        options.append({
                            'name': option_name,
                            'stock': stock,
                            'image_url': image_url
                        })
        return options

    def _extract_material_info(self, soup: BeautifulSoup) -> dict:
        """소재/재료 정보 추출"""
        material_info = {}
        info_items = soup.find_all('div', class_='pro-info-item')

        for item in info_items:
            title_element = item.find('div', class_='pro-info-title')
            info_element = item.find('div', class_='pro-info-info')

            if title_element and info_element:
                title = title_element.get_text(strip=True)
                info = info_element.get_text(strip=True)
                material_info[title] = info

        return material_info

    def _extract_images(self, soup: BeautifulSoup) -> list[str]:
        """상품 이미지 URL 추출"""
        images = []
        img_elements = soup.find_all('img', {'id': re.compile(r'img_translate_\d+')})

        for img in img_elements:
            src = img.get('src', '')
            if src:
                if src.startswith('//'):
                    src = 'https:' + src
                elif src.startswith('/'):
                    src = self.base_url + src
                elif src.startswith('http'):
                    pass
                else:
                    continue
                images.append(src)

        return images

    async def close(self):
        """리소스 정리"""
        if self.use_selenium and hasattr(self, 'driver'):
            try:
                self.driver.quit()
            except Exception:
                pass
        elif hasattr(self, 'client'):
            try:
                await self.client.aclose()
            except Exception:
                pass