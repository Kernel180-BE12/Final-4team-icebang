# app/errors/handlers.py
from fastapi import Request, status
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException
from fastapi.exceptions import RequestValidationError
from .messages import ERROR_MESSAGES, get_error_message

# HTTPException 핸들러 (예: 404, 403 등)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": get_error_message(exc.status_code, str(exc.detail))
        },
    )

# ValidationError 핸들러 (422)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "error": ERROR_MESSAGES[status.HTTP_422_UNPROCESSABLE_ENTITY],
            "errors": exc.errors(),
        },
    )

# 기타 모든 예외
async def unhandled_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "error": ERROR_MESSAGES[status.HTTP_500_INTERNAL_SERVER_ERROR],
        },
    )
