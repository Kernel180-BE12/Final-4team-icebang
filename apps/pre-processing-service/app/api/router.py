from fastapi import APIRouter
from app.decorators.logging import log_api_call

router = APIRouter()

@router.get("/")
async def root():
    return {"message": "Hello World"}


@router.get("/hello/{name}" , tags=["hello"])
# @log_api_call
async def say_hello(name: str):
    return {"message": f"Hello {name}"}

# 이 엔드포인트는 테스트를 위해 예외를 발생시킵니다.
@router.get("/test-error")
def test_error():
    raise ValueError("이것은 테스트용 값 오류입니다.")

# 특정 경로에서 의도적으로 에러 발생
@router.get("/error")
async def trigger_error():
    result = 1 / 0  # ZeroDivisionError 발생
    return {"result": result}