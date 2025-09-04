import os
import time

from selenium.webdriver.support.ui import WebDriverWait
from app.service.crawling_service import CrawlingService


class BlogPostService:

    def __init__(self):
        self.crawling_service = CrawlingService()
        self.driver = self.crawling_service.get_driver()
        self.wait = self.crawling_service.get_wait()
        self.id = os.getenv("BLOG_ID", "fair_05@nate.com")
        self.password = os.getenv("BLOG_PASSWORD", "kdyn264105*")

    def post_content(self,
                     blog_type: str,
                     url: str,
                     title: str,
                     content: str):
        """
        블로그 포스트 작성
        :param blog_type: 블로그 종류
        :param url: 블로그 포스트 URL
        :param title: 블로그 포스트 제목
        :param content: 블로그 포스트 내용
        """

        try:
            self.driver.get("https://www.tistory.com")


if __name__ == "__main__":
    service = BlogPostService()
    service.post_content("tistory", "https://www.tistory.com", "안녕하세요", "Hello World")