from fastapi import APIRouter
from app.services.ChunkingService import ChunkingService

router = APIRouter()

@router.get("/")
async def root():
    return {"message": "Hello World"}


@router.get("/hello/{name}")
# @log_api_call
async def say_hello(name: str):
    return {"message": f"Hello {name}"}

@router.post("/chunk")
async def chunk_text(text: str, chunk_size: int = 100, overlap: int = 20):
    service = ChunkingService()
    chunks = service.chunk_text(text, chunk_size, overlap)
    return {"chunks": chunks, "count": len(chunks)}