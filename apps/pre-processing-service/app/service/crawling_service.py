from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait

class CrawlingService:

    def __init__(self):
        self.options = self._get_chrome_options()
        self.driver = None

    def _get_chrome_options(self):
        """
        크롬 옵션 설정
        1. 헤드리스 모드 비활성화 (네이버 탐지 우회)
        2. 샌드박스 비활성화
        3. GPU 비활성화
        4. 완전한 사용자 에이전트 설정
        5. 자동화 탐지 우회 설정
        """

        options = Options()

        options.add_argument('--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36')
        # options.add_argument('--headless') 백그라운드 실행시 주석 해제
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--disable-gpu")
        options.add_argument("--disable-extensions")
        options.add_experimental_option("excludeSwitches", ["enable-automation"])
        options.add_experimental_option('useAutomationExtension', False)
        options.add_argument("--disable-blink-features=AutomationControlled")

        return options

    def get_driver(self):
        """
        셀레니움 웹 드라이버 반환
        :return: 셀레니움 웹 드라이버
        """

        if self.driver is None:
            self.driver = webdriver.Chrome(options=self.options)

        return self.driver

    def get_wait(self, timeout: int = 15):
        """
        WebDriverWait 객체 반환
        :param timeout: 대기 시간 (초)
        :return: WebDriverWait 객체
        """

        if self.driver is None:
            self.get_driver()

        return WebDriverWait(self.driver, timeout)

    def close(self):
        """
        셀레니움 웹 드라이버 종료
        """

        if self.driver:
            self.driver.quit()
            self.driver = None