from ...service.keyword_service import keyword_search
from fastapi import APIRouter
from ...errors.CustomException import *
from ...model.schemas import RequestNaverSearch, ResponseNaverSearch

router = APIRouter()


@router.post(
    "/search", response_model=ResponseNaverSearch, summary="네이버 키워드 검색"
)
async def search(request: RequestNaverSearch):
    """
    이 엔드포인트는 JSON 요청으로 네이버 키워드 검색을 수행합니다.

    요청 예시:
    {
        "tag": "naver",
        "category": "50000000",
        "start_date": "2025-09-01",
        "end_date": "2025-09-02"
    }
    """
    response_data = await keyword_search(request)
    return response_data
