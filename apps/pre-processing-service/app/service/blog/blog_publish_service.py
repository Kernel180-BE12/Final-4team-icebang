# app/service/blog/blog_publish_service.py
from typing import Dict
from app.errors.CustomException import CustomException
from app.model.schemas import RequestBlogPublish
from app.service.blog.blog_service_factory import BlogServiceFactory


class BlogPublishService:
    """블로그 발행을 담당하는 서비스 클래스"""

    def __init__(self):
        self.factory = BlogServiceFactory()

    def publish_content(self, request: RequestBlogPublish) -> Dict:
        """
        생성된 블로그 콘텐츠를 배포합니다.

        Args:
            request: RequestBlogPublish 객체
        """
        try:
            # 블로그 서비스 생성 (네이버, 티스토리, 블로거 등)
            blog_service = self.factory.create_service(
                request.tag,
                blog_id=request.blog_id,
                blog_password=request.blog_pw,
                blog_name=request.blog_name,
            )

            # 콘텐츠 포스팅
            response_data = blog_service.post_content(
                title=request.post_title,
                content=request.post_content,
                tags=request.post_tags,
            )

            if not response_data:
                raise CustomException(
                    detail=f"{request.tag} 블로그 포스팅에 실패했습니다.",
                    status_code=500,
                    code="POSTING_FAIL"
                )

            return response_data

        except CustomException:
            # 이미 CustomException이면 그대로 전달
            raise
        except Exception as e:
            # 예상치 못한 예외 처리
            raise CustomException(
                detail=f"블로그 포스팅 중 오류가 발생했습니다: {str(e)}",
                status_code=500,
                code="ERROR"
            )
