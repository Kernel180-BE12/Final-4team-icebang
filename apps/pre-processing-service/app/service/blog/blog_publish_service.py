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
        """
        try:
            # 팩토리를 통해 적절한 서비스 생성
            blog_service = self.factory.create_service(request.tag)

            # 공통 인터페이스로 포스팅 실행
            response_data = blog_service.post_content(
                title=request.post_title,
                content=request.post_content,
                tags=request.post_tags,
            )

            if not response_data:
                raise CustomException(
                    f"{request.tag} 블로그 포스팅에 실패했습니다.", status_code=500
                )

            return response_data

        except CustomException:
            # 이미 처리된 예외는 그대로 전달
            raise
        except Exception as e:
            # 예상치 못한 예외 처리
            raise CustomException(
                f"블로그 포스팅 중 오류가 발생했습니다: {str(e)}", status_code=500
            )
