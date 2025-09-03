# app/api/endpoints/keywords.py
from fastapi import APIRouter
from app.decorators.logging import log_api_call
from ...errors.CustomException import *
from fastapi import APIRouter

from ...model.schemas import *
# 이 파일만의 독립적인 라우터를 생성합니다.
router = APIRouter()

@router.get("/")
async def root():
    return {"message": "blog API"}

@router.post("/rag/create", response_model=ResponsetBlogCreate)
async def rag_create(request: RequestBlogCreate):
    return {"message": "blog API"}

@router.post("/publish", response_model=RequestBlogPublish)
async def publish(request: ResponsetBlogPublish):
    return {"message": "blog API"}