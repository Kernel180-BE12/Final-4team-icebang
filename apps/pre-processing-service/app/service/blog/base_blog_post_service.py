from abc import ABC, abstractmethod
from typing import Dict, List, Optional
from app.errors.BlogPostingException import *
from app.service.blog.blog_post_adapter import BlogPostAdapter


class BaseBlogPostService(ABC):
    """
    블로그 포스팅 서비스 베이스 클래스
    어댑터 패턴을 사용하여 각 플랫폼 구현체를 관리
    """

    def __init__(self, adapter: BlogPostAdapter):
        """
        어댑터를 주입받아 초기화
        :param adapter: 플랫폼별 어댑터 구현체
        """
        self.adapter = adapter
        self._initialized = False

    def initialize(self) -> None:
        """서비스 초기화"""
        try:
            self.adapter.initialize()
            self._initialized = True
        except Exception as e:
            raise BlogServiceInitializationException(
                self.adapter.get_platform_name(),
                f"초기화 실패: {str(e)}"
            ) from e

    def is_initialized(self) -> bool:
        """초기화 상태 확인"""
        return self._initialized

    def post_content(self, title: str, content: str, tags: List[str] = None) -> Dict:
        """
        블로그 포스팅 통합 메서드
        """
        if not self._initialized:
            self.initialize()

        # 1. 콘텐츠 유효성 검사
        self.adapter.validate_content(title, content, tags)

        # 2. 인증
        self.adapter.authenticate()

        # 3. 포스트 작성 및 발행
        self.adapter.write_content(title, content, tags)

        # 4. 결과 반환
        return {
            "platform": self.adapter.get_platform_name(),
            "title": title,
            "content_length": len(content),
            "tags": tags or []
        }

    def get_platform_name(self) -> str:
        """플랫폼 이름 반환"""
        return self.adapter.get_platform_name()

    def __del__(self):
        """리소스 정리"""
        if hasattr(self, 'adapter') and self.adapter:
            self.adapter.cleanup()