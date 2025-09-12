import os
from typing import List, Optional
from abc import ABC, abstractmethod
from app.utils.crawling_util import CrawlingUtil
from app.errors.BlogPostingException import *
from app.errors.CrawlingException import *
from app.service.blog.blog_post_adapter import BlogPostAdapter


class SeleniumBlogAdapter(BlogPostAdapter):
    """
    Selenium WebDriver를 사용하는 블로그 플랫폼용 어댑터
    """

    def __init__(self):
        self.crawling_service = None
        self.web_driver = None
        self.wait_driver = None

    def initialize(self) -> None:
        """WebDriver 초기화"""
        try:
            self.crawling_service = CrawlingUtil()
            self.web_driver = self.crawling_service.get_driver()
            self.wait_driver = self.crawling_service.get_wait()
            self._load_config()
        except Exception as e:
            raise WebDriverConnectionException() from e

    @abstractmethod
    def _load_config(self) -> None:
        """플랫폼별 설정 로드"""
        pass

    @abstractmethod
    def authenticate(self) -> None:
        """플랫폼별 로그인 구현"""
        pass

    @abstractmethod
    def write_content(self, title: str, content: str, tags: List[str] = None) -> None:
        """플랫폼별 포스팅 작성 구현"""
        pass

    @abstractmethod
    def get_platform_name(self) -> str:
        """플랫폼 이름 반환"""
        pass

    def validate_content(self, title: str, content: str, tags: Optional[List[str]] = None) -> None:
        """기본 유효성 검사"""
        if not title or not title.strip():
            raise BlogContentValidationException("title", "제목이 비어있습니다")

        if not content or not content.strip():
            raise BlogContentValidationException("content", "내용이 비어있습니다")

    def cleanup(self) -> None:
        """WebDriver 정리"""
        if self.web_driver:
            try:
                self.web_driver.quit()
            except Exception:
                pass