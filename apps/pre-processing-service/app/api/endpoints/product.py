from fastapi import APIRouter, Request, HTTPException
from app.decorators.logging import log_api_call
from ...errors.CustomException import InvalidItemDataException, ItemNotFoundException, CustomException
from ...service.crawl_service import CrawlService
from ...service.search_service import SearchService
from ...service.match_service import MatchService
from ...service.similarity_service import SimilarityService
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
    try:
        search_service = SearchService()
        result = await search_service.search_products(request)

        if not result:
            raise CustomException(500, "상품 검색에 실패했습니다.", "SEARCH_FAILED")

        return result
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/match", response_model=ResponseSadaguMatch)
async def match(request: RequestSadaguMatch):
    """
    상품 매칭 엔드포인트
    """
    try:
        match_service = MatchService()
        result = match_service.match_products(request)

        if not result:
            raise CustomException(500, "상품 매칭에 실패했습니다.", "MATCH_FAILED")

        return result
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/similarity", response_model=ResponseSadaguSimilarity)
async def similarity(request: RequestSadaguSimilarity):
    """
    유사도 분석 엔드포인트
    """
    try:
        similarity_service = SimilarityService()
        result = similarity_service.select_product_by_similarity(request)

        if not result:
            raise CustomException(500, "유사도 분석에 실패했습니다.", "SIMILARITY_FAILED")

        return result
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/crawl", response_model=ResponseSadaguCrawl)
async def crawl(request: Request, body: RequestSadaguCrawl):
    """
    상품 상세 정보 크롤링 엔드포인트
    """
    try:
        crawl_service = CrawlService()
        result = await crawl_service.crawl_product_detail(body)

        if not result:
            raise CustomException(500, "상품 크롤링에 실패했습니다.", "CRAWL_FAILED")

        return result
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except ItemNotFoundException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))