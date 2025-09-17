from ...errors.CustomException import *
from fastapi import APIRouter

from ...model.schemas import *
from app.service.blog.tistory_blog_post_service import TistoryBlogPostService
from app.service.blog.naver_blog_post_service import NaverBlogPostService
from ...service.blog.blogger_blog_post_adapter import (
    BloggerBlogPostAdapter,
)  # 수정된 import
from app.utils.response import Response
from app.service.blog.blog_create_service import BlogContentService

router = APIRouter()


@router.post(
    "/rag/create",
    response_model=ResponseBlogCreate,
    summary="RAG 기반 블로그 콘텐츠 생성",
)
async def rag_create(request: RequestBlogCreate):
    """
    RAG 기반 블로그 콘텐츠 생성
    """
    blog_service = BlogContentService()
    response_data = blog_service.generate_blog_content(request)

    return Response.ok(response_data)

@router.post(
    "/publish",
    response_model=ResponseBlogPublish,
    summary="블로그 콘텐츠 배포 (네이버/티스토리/블로거 지원)",
)
async def publish(request: RequestBlogPublish):
    """
    생성된 블로그 콘텐츠를 배포합니다.
    네이버 블로그와 티스토리 블로그를 지원하며,
    현재는 생성된 콘텐츠가 아닌 임의의 제목, 내용, 태그를 배포합니다.
    """
    if request.tag == "naver":
        naver_service = NaverBlogPostService()
        response_data = naver_service.post_content(
            title=request.post_title,
            content=request.post_content,
            tags=request.post_tags,
        )

        if not response_data:
            raise CustomException(
                "네이버 블로그 포스팅에 실패했습니다.", status_code=500
            )

        return Response.ok(response_data)

    elif request.tag == "tistory":
        tistory_service = TistoryBlogPostService()
        response_data = tistory_service.post_content(
            title=request.post_title,
            content=request.post_content,
            tags=request.post_tags,
        )

        if not response_data:
            raise CustomException(
                "티스토리 블로그 포스팅에 실패했습니다.", status_code=500
            )

        return Response.ok(response_data)

    elif request.tag == "blogger":
        blogger_service = BloggerBlogPostAdapter()  # 수정: Adapter 사용
        response_data = blogger_service.post_content(
            title=request.post_title,
            content=request.post_content,
            tags=request.post_tags,
        )

        if not response_data:
            raise CustomException(
                "블로거 블로그 포스팅에 실패했습니다.", status_code=500
            )

        return Response.ok(response_data)
