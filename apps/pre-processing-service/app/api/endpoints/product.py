from fastapi import APIRouter, Request, HTTPException
from app.decorators.logging import log_api_call
from ...errors.CustomException import InvalidItemDataException, ItemNotFoundException
from ...service.crawl_service import crawl_product_detail
from ...service.search_service import search_products
from ...service.match_service import match_products
from ...service.similarity_service import select_product_by_similarity
from ...model.schemas import *

router = APIRouter()

@router.get("/")
async def root():
    return {"message": "product API"}

@router.post("/search", response_model=ResponseSadaguSearch)
async def search(request: RequestSadaguSearch):
    """
    상품 검색 엔드포인트
    """
    return search_products(request)

@router.post("/match", response_model=ResponseSadaguMatch)
async def match(request: RequestSadaguMatch):
    """
    상품 매칭 엔드포인트
    """
    return match_products(request)

@router.post("/similarity", response_model=ResponseSadaguSimilarity)
async def similarity(request: RequestSadaguSimilarity):
    """
    유사도 분석 엔드포인트
    """
    return select_product_by_similarity(request)

@router.post("/crawl", response_model=ResponseSadaguCrawl)
async def crawl(request: Request, body: RequestSadaguCrawl):
    """
    상품 상세 정보 크롤링 엔드포인트
    """
    try:
        result = await crawl_product_detail(body)
        return result
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except ItemNotFoundException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
