from abc import abstractmethod
from app.errors.BlogPostingException import *
from app.service.blog.blog_post_adapter import BlogPostAdapter

class ApiBlogAdapter(BlogPostAdapter):
    """
    API를 사용하는 블로그 플랫폼용 어댑터
    """

    def __init__(self):
        self.api_client = None
        self.authenticated = False

    def initialize(self) -> None:
        """API 클라이언트 초기화"""
        try:
            self._load_config()
            self._setup_api_client()
        except Exception as e:
            raise BlogServiceInitializationException(
                self.get_platform_name(),
                f"API 초기화 실패: {str(e)}"
            ) from e

    @abstractmethod
    def _load_config(self) -> None:
        """플랫폼별 설정 로드"""
        pass

    @abstractmethod
    def _setup_api_client(self) -> None:
        """API 클라이언트 설정"""
        pass

    @abstractmethod
    def authenticate(self) -> None:
        """API 인증"""
        pass

    @abstractmethod
    def write_content(self, title: str, content: str, tags: List[str] = None) -> None:
        """API를 통한 포스팅 작성"""
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
        """API 클라이언트 정리"""
        if self.api_client:
            try:
                # API 클라이언트별 정리 로직
                pass
            except Exception:
                pass