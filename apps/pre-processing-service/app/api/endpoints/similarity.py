from fastapi import APIRouter
from app.service.similarity_service import select_product_by_similarity
from app.model.schemas import RequestSadaguSimilarity, ResponseSadaguSimilarity

router = APIRouter()

@router.post("/", response_model=ResponseSadaguSimilarity)
async def similarity(request: RequestSadaguSimilarity):
    return select_product_by_similarity(request)
