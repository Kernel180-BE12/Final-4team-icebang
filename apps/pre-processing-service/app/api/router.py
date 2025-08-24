# app/api/router.py
from fastapi import APIRouter
from .endpoints import embedding, processing,test

api_router = APIRouter()

# embedding API URL
api_router.include_router(embedding.router, prefix="/emb", tags=["Embedding"])

# processing API URL
api_router.include_router(processing.router, prefix="/prc", tags=["Processing"])

#모듈 테스터를 위한 endpoint
api_router.include_router(test.router, prefix="/test", tags=["Test"])

@api_router.get("/")
async def root():
    return {"message": "서버 실행중입니다."}