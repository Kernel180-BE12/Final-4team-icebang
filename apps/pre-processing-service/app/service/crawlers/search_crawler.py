import urllib.parse
import time
from .base_crawler import BaseCrawler
from loguru import logger
from bs4 import BeautifulSoup
from selenium.webdriver.common.by import By


class SearchCrawler(BaseCrawler):
    """상품 검색 전용 크롤러"""

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
            return unique_products[:40]

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
            return product_links[:40]

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
