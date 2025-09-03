from fastapi import APIRouter
from app.decorators.logging import log_api_call
from ...errors.CustomException import *
from fastapi import APIRouter

from ...model.schemas import *;

# 이 파일만의 독립적인 라우터를 생성합니다.
router = APIRouter()

@router.post("/crawl",response_model=ResponsetSadaguCrawl)
async def crawl(request: RequestSadaguCrawl):
    return ResponsetSadaguCrawl()
