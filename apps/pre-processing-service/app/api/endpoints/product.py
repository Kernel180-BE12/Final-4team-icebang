from fastapi import APIRouter, Request, HTTPException
from app.decorators.logging import log_api_call
from ...errors.CustomException import (
    InvalidItemDataException,
    ItemNotFoundException,
    CustomException,
)
from ...service.crawl_service import CrawlService
from ...service.search_service import SearchService
from ...service.match_service import MatchService
from ...service.similarity_service import SimilarityService


# from ...service.similarity_service import SimilarityService
from ...model.schemas import *

router = APIRouter()


@router.post("/search", response_model=ResponseSadaguSearch, summary="상품 검색")
async def search(request: RequestSadaguSearch):
    """
    요청된 키워드로 사다구몰 상품을 검색합니다.
    """
    try:
        search_service = SearchService()
        response_data = await search_service.search_products(request)

        if not response_data:
            raise CustomException(500, "상품 검색에 실패했습니다.", "SEARCH_FAILED")

        return response_data
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/match", response_model=ResponseSadaguMatch, summary="상품 매칭")
async def match(request: RequestSadaguMatch):
    """
    검색 결과 상품과 키워드를 기반으로 매칭을 수행합니다.
    """
    try:
        match_service = MatchService()
        response_data = match_service.match_products(request)

        if not response_data:
            raise CustomException(500, "상품 매칭에 실패했습니다.", "MATCH_FAILED")

        return response_data
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post(
    "/similarity", response_model=ResponseSadaguSimilarity, summary="상품 유사도 분석"
)
async def similarity(request: RequestSadaguSimilarity):
    """
    매칭된 상품들 중 키워드와의 유사도를 계산하여 최적의 상품을 선택합니다.
    """
    try:
        similarity_service = SimilarityService()
        response_data = similarity_service.select_product_by_similarity(request)

        if not response_data:
            raise CustomException(
                500, "유사도 분석에 실패했습니다.", "SIMILARITY_FAILED"
            )

        return response_data
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post(
    "/crawl", response_model=ResponseSadaguCrawl, summary="상품 상세 정보 크롤링"
)
async def crawl(body: RequestSadaguCrawl):
    """
    상품 상세 페이지를 크롤링하여 상세 정보를 수집합니다.
    """
    try:
        crawl_service = CrawlService()
        response_data = await crawl_service.crawl_product_detail(body)

        if not response_data:
            raise CustomException(500, "상품 크롤링에 실패했습니다.", "CRAWL_FAILED")

        return response_data
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except ItemNotFoundException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
