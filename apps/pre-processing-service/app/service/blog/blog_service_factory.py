from typing import Dict, Type
from app.service.blog.base_blog_post_service import BaseBlogPostService
from app.service.blog.naver_blog_post_service import NaverBlogPostService
from app.service.blog.tistory_blog_post_service import TistoryBlogPostService
from app.service.blog.blogger_blog_post_adapter import BloggerBlogPostAdapter
from app.errors.CustomException import CustomException


class BlogServiceFactory:
    """블로그 서비스 객체 생성을 담당하는 팩토리"""

    # 서비스 타입별 클래스 매핑
    _services: Dict[str, Type[BaseBlogPostService]] = {
        "naver": NaverBlogPostService,
        "tistory": TistoryBlogPostService,
        "blogger": BloggerBlogPostAdapter,
    }

    @classmethod
    def create_service(cls, platform: str) -> BaseBlogPostService:
        """
        플랫폼에 따른 블로그 서비스 인스턴스 생성
        """
        service_class = cls._services.get(platform.lower())

        if not service_class:
            raise CustomException(
                f"지원하지 않는 플랫폼입니다: {platform}. "
                f"지원 플랫폼: {list(cls._services.keys())}",
                status_code=400,
            )

        return service_class()

    @classmethod
    def get_supported_platforms(cls) -> list:
        """지원하는 플랫폼 목록 반환"""
        return list(cls._services.keys())
