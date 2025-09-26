# app/api/router.py
from fastapi import APIRouter
from .endpoints import keywords, blog, product, test, sample, ocr  # ocr 추가
from ..core.config import settings

api_router = APIRouter()

api_router.include_router(keywords.router, prefix="/keywords", tags=["keyword"])
api_router.include_router(blog.router, prefix="/blogs", tags=["blog"])
api_router.include_router(product.router, prefix="/products", tags=["product"])
api_router.include_router(test.router, prefix="/tests", tags=["Test"])
api_router.include_router(sample.router, prefix="/v0", tags=["Sample"])

# OCR 라우터 추가
api_router.include_router(ocr.router, prefix="/ocr", tags=["OCR"])

# 기존 엔드포인트들...
@api_router.get("/ping")
async def root():
    return {"message": "서버 실행중입니다."}

@api_router.get("/db")
def get_settings():
    return {"환경": settings.env_name, "데이터베이스 URL": settings.db_url}