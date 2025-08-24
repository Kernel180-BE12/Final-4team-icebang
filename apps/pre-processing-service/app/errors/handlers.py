# app/errors/handlers.py
from fastapi import Request, status
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException
from fastapi.exceptions import RequestValidationError
from .messages import ERROR_MESSAGES, get_error_message
from ..errors.CustomException import CustomException

# CustomException 핸들러
async def custom_exception_handler(request: Request, exc: CustomException):
    """
    CustomException을 상속받는 모든 예외를 처리합니다.
    """
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error_code": exc.code,
            "message": exc.detail,
        },
    )

# FastAPI의 HTTPException 핸들러 (예: 404 Not Found)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    """
    FastAPI에서 기본적으로 발생하는 HTTP 관련 예외를 처리합니다.
    """
    if exc.status_code == status.HTTP_404_NOT_FOUND:
        # 404 에러의 경우, FastAPI의 기본 "Not Found" 메시지 대신 우리가 정의한 메시지를 사용합니다.
        message = ERROR_MESSAGES.get(exc.status_code, "요청하신 리소스를 찾을 수 없습니다.")
    else:
        # 다른 HTTP 예외들은 FastAPI가 제공하는 detail 메시지를 우선적으로 사용합니다.
        message = get_error_message(exc.status_code, exc.detail)

    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error_code": f"HTTP_{exc.status_code}",
            "message": message
        },
    )

# Pydantic Validation Error 핸들러 (422)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """
    Pydantic 모델 유효성 검사 실패 시 발생하는 예외를 처리합니다.
    """
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "error_code": "VALIDATION_ERROR",
            "message": ERROR_MESSAGES[status.HTTP_422_UNPROCESSABLE_ENTITY],
            "details": exc.errors(),
        },
    )

# 처리되지 않은 모든 예외 핸들러 (500)
async def unhandled_exception_handler(request: Request, exc: Exception):
    # ...
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "error_code": "INTERNAL_SERVER_ERROR",
            "message": ERROR_MESSAGES[status.HTTP_500_INTERNAL_SERVER_ERROR],
        },
    )
