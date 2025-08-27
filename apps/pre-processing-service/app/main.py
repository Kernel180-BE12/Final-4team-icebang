# main.py
import uvicorn
from fastapi import FastAPI
from starlette.exceptions import HTTPException as StarletteHTTPException
from fastapi.exceptions import RequestValidationError

# --- 애플리케이션 구성 요소 임포트 ---
from app.api.router import api_router
from app.middleware.logging import LoggingMiddleware
from app.errors.CustomException import *
from app.errors.handlers import *

# --- FastAPI 애플리케이션 인스턴스 생성 ---
app = FastAPI(
    title="pre-processing-service",
    description="",
    version="1.0.0"
)

# --- 예외 핸들러 등록 ---
# 등록 순서가 중요합니다: 구체적인 예외부터 등록하고 가장 일반적인 예외(Exception)를 마지막에 등록합니다.
app.add_exception_handler(CustomException, custom_exception_handler)
app.add_exception_handler(StarletteHTTPException, http_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(Exception, unhandled_exception_handler)

# --- 미들웨어 등록 ---
app.add_middleware(LoggingMiddleware)

# --- 라우터 등록 ---
app.include_router(api_router, prefix="", tags=["api"])

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
