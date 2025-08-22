from fastapi import APIRouter
from app.decorators.logging import log_api_call

router = APIRouter()

@router.get("/")
async def root():
    return {"message": "Hello World"}


@router.get("/hello/{name}")
# @log_api_call
async def say_hello(name: str):
    return {"message": f"Hello {name}"}

