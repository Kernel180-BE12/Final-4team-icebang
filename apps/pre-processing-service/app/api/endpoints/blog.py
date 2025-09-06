from ...errors.CustomException import *
from fastapi import APIRouter

from ...model.schemas import *
from ...service.tistory_blog_post_service import TistoryBlogPostService
from ...service.naver_blog_post_service import NaverBlogPostService

# 이 파일만의 독립적인 라우터를 생성합니다.
router = APIRouter()

@router.get("/")
async def root():
    return {"message": "blog API"}

@router.post("/rag/create", response_model=ResponseBlogCreate)
async def rag_create(request: RequestBlogCreate):
    """
    RAG 기반 블로그 콘텐츠 생성
    """
    return {"message": "blog API"}

@router.post("/publish", response_model=ResponseBlogPublish)
async def publish(request: RequestBlogPublish):
    """
    생성된 블로그 콘텐츠 배포
    네이버 블로그와 티스토리 블로그를 지원
    현재는 생성된 콘텐츠가 아닌, 임의의 제목,내용,태그를 배포
    :param request: RequestBlogPublish
    :return: ResponseBlogPublish
    """

    if request.tag == "naver":
        naver_service = NaverBlogPostService()
        result = naver_service.post_content(
            title=request.title,
            content=request.content,
            tags=request.tags
        )

        if not result:
            raise CustomException("네이버 블로그 포스팅에 실패했습니다.", status_code=500)
        return ResponseBlogPublish(
            job_id= 1,
            schedule_id= 1,
            schedule_his_id= 1,
            status="200",
            metadata=result
        )

    else:
        tistory_service = TistoryBlogPostService()
        result = tistory_service.post_content(
            title=request.title,
            content=request.content,
            tags=request.tags
        )

        if not result:
            raise CustomException("티스토리 블로그 포스팅에 실패했습니다.", status_code=500)

        return ResponseBlogPublish(
            job_id= 1,
            schedule_id= 1,
            schedule_his_id= 1,
            status="200",
            metadata=result
        )