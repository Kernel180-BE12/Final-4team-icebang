from fastapi import APIRouter
from app.service.match_service import match_products
from app.model.schemas import RequestSadaguMatch, ResponseSadaguMatch

router = APIRouter()

@router.post("/", response_model=ResponseSadaguMatch)
async def match(request: RequestSadaguMatch):
    return match_products(request)