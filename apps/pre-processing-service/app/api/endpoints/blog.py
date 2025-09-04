from ...errors.CustomException import *
from fastapi import APIRouter

from ...model.schemas import *
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

@router.post("/publish", response_model=RequestBlogPublish)
async def publish(request: ResponseBlogPublish):
    """
    생성된 블로그 콘텐츠 배포
    """
    return {"message": "blog API"}