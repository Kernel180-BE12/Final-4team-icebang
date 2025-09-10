from abc import ABC, abstractmethod
from typing import Dict, List, Optional

from app.utils.crawling_util import CrawlingUtil
from app.errors.BlogPostingException import *
from app.errors.CrawlingException import *


class BaseBlogPostService(ABC):
    """
    블로그 포스팅 서비스 추상 클래스
    """

    def __init__(self, config_file="blog_config.json"):
        """공통 초기화 로직"""
        # Selenium 기반 서비스를 위한 초기화
        if self._requires_webdriver():
            try:
                self.crawling_service = CrawlingUtil()
                self.web_driver = self.crawling_service.get_driver()
                self.wait_driver = self.crawling_service.get_wait()
            except Exception:
                raise WebDriverConnectionException()
        else:
            # API 기반 서비스의 경우 WebDriver가 필요 없음
            self.crawling_service = None
            self.web_driver = None
            self.wait_driver = None

        # API 기반 서비스를 위한 초기화
        self.config_file = config_file
        self.config = {}
        self.current_upload_account = None

        # API 관련 속성들 (사용하지 않는 서비스에서는 None으로 유지)
        self.blogger_service = None
        self.blog_id = None
        self.scopes = None

        self._load_config()

    def _requires_webdriver(self) -> bool:
        """
        서브클래스에서 WebDriver가 필요한지 여부를 반환
        기본값은 True (Selenium 기반), API 기반 서비스에서는 False로 오버라이드
        """
        return True

    @abstractmethod
    def _load_config(self) -> None:
        """플랫폼별 설정 로드"""
        pass

    def _login(self) -> None:
        """
        플랫폼별 로그인 구현 (API 기반 서비스의 경우 인증으로 대체)
        기본 구현은 아무것도 하지 않음 (API 서비스용)
        """
        pass

    @abstractmethod
    def _write_content(self, title: str, content: str, tags: List[str] = None) -> None:
        """
        플랫폼별 포스팅 작성 구현
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
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

        # 2. 로그인 (Selenium 기반) 또는 인증 (API 기반)
        self._login()

        # 3. 포스트 작성 및 발행
        self._write_content(title, content, tags)

        # 4. 결과 반환
        return {
            "platform": self._get_platform_name(),
            "title": title,
            "content_length": len(content),
            "tags": tags or [],
        }

    def __del__(self):
        """공통 리소스 정리"""
        if hasattr(self, "web_driver") and self.web_driver:
            self.web_driver.quit()
