from fastapi import APIRouter
from ...model.schemas import *

router = APIRouter()


@router.get("/")
async def root():
    return {"message": "sample API"}


@router.post("/keywords/search", summary="네이버 키워드 검색")
async def search(request: RequestNaverSearch):
    return "hello world"



@router.post("/blogs/rag/create", summary="RAG 기반 블로그 콘텐츠 생성")
async def rag_create(request: RequestBlogCreate):
    return "hello world"


@router.post("/blogs/publish", summary="블로그 콘텐츠 배포")
async def publish(request: RequestBlogPublish):
    return "hello world"


@router.post("/products/search", summary="상품 검색")
async def product_search(request: RequestSadaguSearch):
    return "hello world"


@router.post("/products/match", summary="상품 매칭")
async def product_match(request: RequestSadaguMatch):
    return "hello world"


@router.post("/products/similarity", summary="상품 유사도 분석")
async def product_similarity(request: RequestSadaguSimilarity):
    return "hello world"


@router.post("/products/crawl", summary="상품 상세 정보 크롤링")
async def product_crawl(request: RequestSadaguCrawl):
    return "hello world"