# main.py
from fastapi import FastAPI, Request
from loguru import logger
import uvicorn
from starlette import status
from starlette.responses import JSONResponse

from app.api.router import router as api_router
from app.middleware.logging import LoggingMiddleware
# FastAPI 애플리케이션 인스턴스를 생성합니다.
app = FastAPI(
    title="pre-processing-service",
    description="",
    version="1.0.0"
)
#미들 웨어 등록
app.add_middleware(LoggingMiddleware)
#라우터 등록
app.include_router(api_router, prefix="", tags=["api"])

# 모든 Exception을 처리하는 글로벌 핸들러
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.error(f"에러발생{exc}")
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={"message": f"서버 내부 오류가 발생했습니312312321다: {exc}"},
    )

# if __name__ == "__main__":
    # uvicorn.run(app, host="0.0.0.0", port=8000)
