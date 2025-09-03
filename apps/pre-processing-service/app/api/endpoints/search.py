from fastapi import APIRouter
from app.service.search_service import search_products
from app.model.schemas import RequestSadaguSearch, ResponseSadaguSearch

router = APIRouter()

@router.post("/", response_model=ResponseSadaguSearch)
async def search(request: RequestSadaguSearch):
    return search_products(request)
