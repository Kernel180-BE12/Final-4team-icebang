import httpx
import time
from abc import ABC, abstractmethod
from bs4 import BeautifulSoup
from loguru import logger
from app.utils.crawling_util import CrawlingUtil


class BaseCrawler(ABC):
    """크롤러 기본 클래스"""

    def __init__(self, use_selenium: bool = True, headless: bool = True):
        self.base_url = "https://ssadagu.kr"
        self.use_selenium = use_selenium

        if use_selenium:
            self._setup_selenium(headless)
        else:
            self._setup_httpx()

    def _setup_selenium(self, headless: bool):
        """Selenium WebDriver 초기화"""
        try:
            self.crawling_util = CrawlingUtil(headless=headless)
            self.driver = self.crawling_util.get_driver()
            self.wait = self.crawling_util.get_wait()
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

    async def close(self):
        """리소스 정리"""
        if self.use_selenium and hasattr(self, 'crawling_util'):
            try:
                self.crawling_util.close()
                logger.info("Selenium WebDriver 종료 완료")
            except Exception as e:
                logger.warning(f"Selenium WebDriver 종료 중 오류: {e}")
        elif hasattr(self, 'client'):
            try:
                await self.client.aclose()
                logger.info("httpx 클라이언트 종료 완료")
            except Exception as e:
                logger.warning(f"httpx 클라이언트 종료 중 오류: {e}")