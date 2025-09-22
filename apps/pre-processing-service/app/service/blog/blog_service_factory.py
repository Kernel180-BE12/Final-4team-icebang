from typing import Dict, Type, Optional
from app.service.blog.base_blog_post_service import BaseBlogPostService
from app.service.blog.naver_blog_post_service import NaverBlogPostService
from app.service.blog.tistory_blog_post_service import TistoryBlogPostService
from app.service.blog.blogger_blog_post_adapter import BloggerBlogPostAdapter
from app.errors.CustomException import CustomException


class BlogServiceFactory:
    """블로그 서비스 객체 생성을 담당하는 팩토리"""

    # 서비스 타입별 클래스 매핑
    _services: Dict[str, Type[BaseBlogPostService]] = {
        "naver_blog": NaverBlogPostService,
        "tistory_blog": TistoryBlogPostService,
        "blogger": BloggerBlogPostAdapter,
    }

    @classmethod
    def create_service(
        cls,
        platform: str,
        blog_id: str,
        blog_password: str,
        blog_name: Optional[str] = None,
    ) -> BaseBlogPostService:
        """
        플랫폼에 따른 블로그 서비스 인스턴스 생성

        Args:
            platform: 블로그 플랫폼 (naver, tistory, blogger)
            blog_id: 블로그 아이디
            blog_password: 블로그 비밀번호
        """
        service_class = cls._services.get(platform.lower())

        if not service_class:
            raise CustomException(
                f"지원하지 않는 플랫폼입니다: {platform}. "
                f"지원 플랫폼: {list(cls._services.keys())}",
                status_code=400,
            )

        # 각 서비스의 설정을 의존성 주입
        if platform.lower() == "tistory_blog":
            if not blog_name:
                raise CustomException(
                    200,
                    "티스토리 블로그가 존재하지않습니다.",
                    "NOT_FOUND_BLOG",
                )
            return service_class(blog_id, blog_password, blog_name)
        if platform.lower() == "blogger":
            return service_class()
        return service_class(blog_id, blog_password)

    @classmethod
    def get_supported_platforms(cls) -> list:
        """지원하는 플랫폼 목록 반환"""
        return list(cls._services.keys())
