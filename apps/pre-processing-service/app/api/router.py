# app/api/router.py
from fastapi import APIRouter
from .endpoints import keywords, blog, product, test
from ..core.config import settings

api_router = APIRouter()

# embedding API URL
api_router.include_router(keywords.router, prefix="/keywords", tags=["keyword"])

# processing API URL
api_router.include_router(blog.router, prefix="/blogs", tags=["blog"])

# 상품 API URL
api_router.include_router(product.router, prefix="/products", tags=["product"])

# 모듈 테스터를 위한 endpoint -> 추후 삭제 예정
api_router.include_router(test.router, prefix="/tests", tags=["Test"])


@api_router.get("/")
async def root():
    return {"message": "서버 실행중입니다."}


@api_router.get("/db")
def get_settings():
    """
    환경 변수가 올바르게 로드되었는지 확인하는 엔드포인트
    """
    return {"환경": settings.env_name, "데이터베이스 URL": settings.db_url}
