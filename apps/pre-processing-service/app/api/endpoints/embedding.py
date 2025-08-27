# app/api/endpoints/embedding.py
from fastapi import APIRouter
from app.decorators.logging import log_api_call
from ...errors.CustomException import *
from fastapi import APIRouter

# 이 파일만의 독립적인 라우터를 생성합니다.
router = APIRouter()

@router.get("/")
async def root():
    return {"message": "Items API"}