# app/api/endpoints/keywords.py
from ...service.keyword_service import keyword_search

from fastapi import APIRouter
from app.decorators.logging import log_api_call
from ...errors.CustomException import *
from fastapi import APIRouter
from ...errors.CustomException import  *
from ...model.schemas import RequestNaverSearch, ResponseNaverSearch, RequestSadaguValidate, ResponsetSadaguValidate

# 이 파일만의 독립적인 라우터를 생성합니다.
router = APIRouter()

@router.get("/")
async def root():
    return {"message": "Items API"}

@router.post("/search",response_model=ResponseNaverSearch)
async def search(request: RequestNaverSearch):
    """
    이 엔드포인트는 아래와 같은 JSON 요청을 받습니다.
    RequestBase와 RequestNaverSearch의 모든 필드를 포함해야 합니다.
    {
        "job_id": "job-123",
        "schedule_id": "schedule-456",
        "schedule_his_id": 789,
        "tag": "fastapi",
        "category": "tech",
        "start_date": "2025-09-01T12:00:00",
        "end_date": "2025-09-02T15:00:00"
    }
    """
    response_data= await keyword_search(request)
    return response_data

@router.post("/ssadagu/validate",response_model=ResponseNaverSearch)
async def ssadagu_validate(request: RequestNaverSearch):
    return ResponseNaverSearch()
