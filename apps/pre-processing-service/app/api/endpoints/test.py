# app/api/endpoints/embedding.py
from fastapi import APIRouter
from app.decorators.logging import log_api_call
from ...errors.CustomException import *
from fastapi import APIRouter

# 이 파일만의 독립적인 라우터를 생성합니다.
router = APIRouter()

@router.get("/")
async def root():
    return {"message": "테스트 API"}


@router.get("/hello/{name}" , tags=["hello"])
# @log_api_call
async def say_hello(name: str):
    return {"message": f"Hello {name}"}


# 특정 경로에서 의도적으로 에러 발생
#커스텀에러 테스터 url
@router.get("/error/{item_id}")
async def trigger_error(item_id: int):
    if item_id == 0:
        raise InvalidItemDataException()

    if item_id == 404:
        raise ItemNotFoundException(item_id=item_id)

    if item_id == 500:
        raise ValueError("이것은 테스트용 값 오류입니다.")


    return {"result": item_id}