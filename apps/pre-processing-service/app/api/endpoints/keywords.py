# app/api/endpoints/keywords.py
from ...service.keyword_service import keyword_search

from fastapi import APIRouter
from ...errors.CustomException import *
from ...model.schemas import RequestNaverSearch, ResponseNaverSearch

# 이 파일만의 독립적인 라우터를 생성합니다.
router = APIRouter()


@router.get("/")
async def root():
    return {"message": "keyword API"}


@router.post("/search", response_model=ResponseNaverSearch)
async def search(request: RequestNaverSearch):
    """
    이 엔드포인트는 아래와 같은 JSON 요청을 받습니다.
    RequestBase와 RequestNaverSearch의 모든 필드를 포함해야 합니다.
    {
        "job_id":1,
        "schedule_id": 1,
        "sschdule_his_id":1,
        "tag":"naver",
        "category":"50000000",
        "start_date":"2025-09-01",
        "end_date":"2025-09-02"
    }
    """
    response_data = await keyword_search(request)
    return response_data


@router.post("/ssadagu/validate", response_model=ResponseNaverSearch)
async def ssadagu_validate(request: RequestNaverSearch):
    return ResponseNaverSearch()
