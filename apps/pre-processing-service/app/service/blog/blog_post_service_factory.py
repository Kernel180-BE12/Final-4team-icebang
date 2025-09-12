from .base_blog_post_service import BaseBlogPostService
from .adapters.naver_blog_adapter import NaverBlogAdapter
from .adapters.blogger_blog_adapter import BloggerBlogAdapter
from .adapters.tistory_blog_adapter import TistoryBlogAdapter


class BlogPostServiceFactory:
    """
    블로그 포스팅 서비스 팩토리
    """

    @staticmethod
    def create_service(platform: str) -> BaseBlogPostService:
        """
        플랫폼에 따른 서비스 생성
        """
        if platform.upper() == "NAVER":
            adapter = NaverBlogAdapter()
        elif platform.upper() == "BLOGGER":
            adapter = BloggerBlogAdapter()
        elif platform.upper() == "TISTORY":
            adapter = TistoryBlogAdapter()
        else:
            raise ValueError(f"지원하지 않는 플랫폼입니다: {platform}")

        return BaseBlogPostService(adapter)