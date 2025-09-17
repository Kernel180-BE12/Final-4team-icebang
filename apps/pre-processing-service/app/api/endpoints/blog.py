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
from app.service.blog.blog_publish_service import BlogPublishService

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
    publish_service = BlogPublishService()
    response_data = publish_service.publish_content(request)

    return Response.ok(response_data)
