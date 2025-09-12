from abc import ABC, abstractmethod
from typing import Dict, List, Optional


class BlogPostAdapter(ABC):
    """
    블로그 포스팅 어댑터 인터페이스
    각 플랫폼별 구현체가 이를 상속받아 구현
    """

    @abstractmethod
    def initialize(self) -> None:
        """플랫폼별 초기화"""
        pass

    @abstractmethod
    def authenticate(self) -> None:
        """플랫폼별 인증/로그인"""
        pass

    @abstractmethod
    def write_content(self, title: str, content: str, tags: List[str] = None) -> None:
        """플랫폼별 컨텐츠 작성"""
        pass

    @abstractmethod
    def get_platform_name(self) -> str:
        """플랫폼 이름 반환"""
        pass

    @abstractmethod
    def validate_content(self, title: str, content: str, tags: Optional[List[str]] = None) -> None:
        """컨텐츠 유효성 검사"""
        pass

    @abstractmethod
    def cleanup(self) -> None:
        """리소스 정리"""
        pass
