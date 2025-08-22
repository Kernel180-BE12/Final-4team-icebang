# main.py

from fastapi import FastAPI

# app/api/v1/items.py에서 라우터를 임포트합니다.
from app.api.router import router as api_router
from app.middleware.logging import LoggingMiddleware

# FastAPI 애플리케이션 인스턴스를 생성합니다.
app = FastAPI(
    title="My FastAPI Project",
    description="A demonstration of a well-structured FastAPI project.",
    version="1.0.0"
)
app.add_middleware(LoggingMiddleware)
# APIRouter를 메인 앱에 포함시킵니다.
app.include_router(api_router, prefix="", tags=["api"])