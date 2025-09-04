import urllib.parse
import httpx
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.common.exceptions import TimeoutException, NoSuchElementException
import time


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
        chrome_options.add_argument('--headless')
        chrome_options.add_argument('--no-sandbox')
        chrome_options.add_argument('--disable-dev-shm-usage')
        chrome_options.add_argument('--disable-gpu')
        chrome_options.add_argument('--window-size=1920,1080')
        chrome_options.add_argument(
            '--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        )

        try:
            self.driver = webdriver.Chrome(options=chrome_options)
            self.wait = WebDriverWait(self.driver, 10)
            print("Selenium WebDriver 초기화 완료")
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

    async def search_products_selenium(self, keyword: str) -> list[dict]:
        """Selenium을 사용한 상품 검색"""
        encoded_keyword = urllib.parse.quote(keyword)
        search_url = f"{self.base_url}/shop/search.php?ss_tx={encoded_keyword}"

        try:
            self.driver.get(search_url)
            time.sleep(5)

            product_links = []
            link_elements = self.driver.find_elements(By.TAG_NAME, "a")

            for element in link_elements:
                href = element.get_attribute('href')
                if href and 'view.php' in href and ('platform=1688' in href or 'num_iid' in href):
                    # 기본 정보 추출 시도
                    try:
                        title = element.get_attribute('title') or element.text.strip()
                        if title:
                            product_links.append({
                                'url': href,
                                'title': title
                            })
                    except:
                        product_links.append({
                            'url': href,
                            'title': 'Unknown Title'
                        })

            # 중복 제거
            seen_urls = set()
            unique_products = []
            for product in product_links:
                if product['url'] not in seen_urls:
                    seen_urls.add(product['url'])
                    unique_products.append(product)

            print(f"Selenium으로 발견한 상품 링크: {len(unique_products)}개")
            return unique_products[:20]  # 최대 20개로 제한

        except Exception as e:
            print(f"Selenium 검색 오류: {e}")
            return []

    async def search_products_httpx(self, keyword: str) -> list[dict]:
        """httpx를 사용한 상품 검색"""
        encoded_keyword = urllib.parse.quote(keyword)
        search_url = f"{self.base_url}/shop/search.php?ss_tx={encoded_keyword}"

        try:
            response = await self.client.get(search_url)
            response.raise_for_status()
            soup = BeautifulSoup(response.content, 'html.parser')

            product_links = []
            all_links = soup.find_all('a', href=True)

            for link in all_links:
                href = link['href']
                if 'view.php' in href and ('platform=1688' in href or 'num_iid' in href):
                    full_url = f"{self.base_url}{href}" if href.startswith('/') else href
                    title = link.get('title', '') or link.get_text(strip=True) or 'Unknown Title'

                    product_links.append({
                        'url': full_url,
                        'title': title
                    })

            print(f"httpx로 발견한 상품 링크: {len(product_links)}개")
            return product_links[:20]  # 최대 20개로 제한

        except Exception as e:
            print(f"httpx 검색 오류: {e}")
            return []

    async def get_basic_product_info(self, product_url: str) -> dict:
        """기본 상품 정보만 크롤링"""
        try:
            if self.use_selenium:
                self.driver.get(product_url)
                self.wait.until(lambda driver: driver.execute_script("return document.readyState") == "complete")
                soup = BeautifulSoup(self.driver.page_source, 'html.parser')
            else:
                response = await self.client.get(product_url)
                response.raise_for_status()
                soup = BeautifulSoup(response.content, 'html.parser')

            # 제목 추출
            title_element = soup.find('h1', {'id': 'kakaotitle'})
            title = title_element.get_text(strip=True) if title_element else "제목 없음"

            return {
                'url': product_url,
                'title': title
            }

        except Exception as e:
            print(f"기본 상품 크롤링 오류 ({product_url}): {e}")
            return None

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