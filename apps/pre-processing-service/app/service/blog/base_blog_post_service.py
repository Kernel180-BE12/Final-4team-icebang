from abc import ABC, abstractmethod
from typing import Dict

from app.utils.crawling_util import CrawlingUtil
from app.errors.BlogPostingException import *
from app.errors.CrawlingException import *


class BaseBlogPostService(ABC):
    """
    블로그 포스팅 서비스 추상 클래스
    """

    def __init__(self, use_webdriver=True):
        """
        공통 초기화 로직
        :param use_webdriver: 웹드라이버 사용 여부 (API 서비스의 경우 False)
        """
        self.use_webdriver = use_webdriver

        if self.use_webdriver:
            try:
                # 블로그 포스팅용 설정으로 초기화
                self.crawling_service = CrawlingUtil(
                    headless=False,  # 네이버 탐지 우회를 위해 headless 비활성화
                    for_blog_posting=True,
                )
                self.web_driver = self.crawling_service.get_driver()
                self.wait_driver = self.crawling_service.get_wait()
            except Exception:
                raise WebDriverConnectionException()
        else:
            self.crawling_service = None
            self.web_driver = None
            self.wait_driver = None

        self._load_config()

    @abstractmethod
    def _load_config(self) -> None:
        """플랫폼별 설정 로드"""
        pass

    @abstractmethod
    def _login(self) -> None:
        """플랫폼별 로그인 구현"""
        pass

    @abstractmethod
    def _write_content(self, title: str, content: str, tags: List[str] = None) -> str:
        """
        플랫폼별 포스팅 작성 구현
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
        :return: 발행된 블로그 포스트 URL
        """
        pass

    @abstractmethod
    def _get_platform_name(self) -> str:
        """플랫폼 이름 반환"""
        pass

    @abstractmethod
    def _validate_content(
        self, title: str, content: str, tags: Optional[List[str]] = None
    ) -> None:
        """
        공통 유효성 검사 로직
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
        """
        # if not title or not title.strip():
        #     raise BlogContentValidationException("title", "제목이 비어있습니다")
        #
        # if not content or not content.strip():
        #     raise BlogContentValidationException("content", "내용이 비어있습니다")
        #
        # if tags is None:
        #     raise BlogContentValidationException("tags", "태그가 비어있습니다")
        pass

    def post_content(self, title: str, content: str, tags: List[str] = None) -> Dict:
        """
        블로그 포스팅 통합 메서드
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
        :return: 포스팅 결과 요약 딕셔너리
        """
        # 1. 콘텐츠 유효성 검사
        self._validate_content(title, content, tags)

        # 2. 로그인
        self._login()

        # 3. 포스트 작성 및 발행
        post_url = self._write_content(title, content, tags)

        # 4. 결과 반환
        return {
            "tag": self._get_platform_name(),
            "post_title": title,
            "content_length": len(content),
            "tags": tags or [],
            "publish_success": True,
            "post_url": post_url,
        }

    def __del__(self):
        """공통 리소스 정리"""
        if hasattr(self, "web_driver") and self.web_driver:
            self.web_driver.quit()
