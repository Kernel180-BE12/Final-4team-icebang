from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from typing import Optional


class CrawlingUtil:
    """
    공통 Selenium WebDriver 유틸리티
    블로그 포스팅과 상품 크롤링 모두 지원
    """

    def __init__(self, headless: bool = True, for_blog_posting: bool = False):
        """
        :param headless: 헤드리스 모드 사용 여부
        :param for_blog_posting: 블로그 포스팅용 설정 사용 여부
        """
        self.headless = headless
        self.for_blog_posting = for_blog_posting
        self.options = self._get_chrome_options()
        self.driver = None

    def _get_chrome_options(self) -> Options:
        """크롬 옵션 설정"""
        options = Options()

        # 기본 설정
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--disable-gpu")
        options.add_argument("--disable-extensions")

        # 헤드리스 모드 설정
        if self.headless:
            options.add_argument("--headless")
            options.add_argument("--window-size=1920,1080")

        # 블로그 포스팅용 특별 설정 (네이버 탐지 우회)
        if self.for_blog_posting:
            options.add_argument(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            )
            options.add_experimental_option("excludeSwitches", ["enable-automation"])
            options.add_experimental_option("useAutomationExtension", False)
            options.add_argument("--disable-blink-features=AutomationControlled")
        else:
            # 일반 크롤링용 설정
            options.add_argument(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )

        return options

    def get_driver(self) -> webdriver.Chrome:
        """셀레니움 웹 드라이버 반환"""
        if self.driver is None:
            self.driver = webdriver.Chrome(options=self.options)
        return self.driver

    def get_wait(self, timeout: int = 15) -> WebDriverWait:
        """WebDriverWait 객체 반환"""
        if self.driver is None:
            self.get_driver()
        return WebDriverWait(self.driver, timeout)

    def close(self):
        """드라이버 종료"""
        if self.driver:
            self.driver.quit()
            self.driver = None
