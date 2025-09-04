from fastapi import APIRouter, Request, HTTPException
from app.decorators.logging import log_api_call
from ...errors.CustomException import InvalidItemDataException, ItemNotFoundException
from ...service.crawl_service import crawl_product_detail
from ...model.schemas import *

router = APIRouter()

@router.post("/crawl", response_model=ResponseSadaguCrawl)
@log_api_call
async def crawl(request: Request, body: RequestSadaguCrawl):
    try:
        result = await crawl_product_detail(body)
        return result
    except InvalidItemDataException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except ItemNotFoundException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)
    except Exception as e:
        # 나머지 예외는 500으로 처리
        raise HTTPException(status_code=500, detail=str(e))
