
import time
from fastapi import Request
from loguru import logger
from starlette.middleware.base import BaseHTTPMiddleware

class LoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        start_time = time.time()

        # 1. 요청 시작 로그
        logger.info(
            "요청 시작: IP='{}' 메서드='{}' URL='{}'",
            request.client.host, request.method, request.url.path
        )

        try:
            # 2. 다음 미들웨어 또는 최종 엔드포인트 함수 실행
            response = await call_next(request)

            # 3. 요청 성공 시 로그
            process_time = time.time() - start_time
            logger.info(
                "요청 성공: 메서드='{}' URL='{}' 상태코드='{}' (처리 시간: {:.4f}s)",
                request.method, request.url.path, response.status_code, process_time
            )
            return response

        except Exception as e:
            # 4. 예외 발생 시 로그
            process_time = time.time() - start_time
            logger.error(
                "요청 실패: IP='{}' 메서드='{}' URL='{}' 예외='{}' (처리 시간: {:.4f}s)",
                request.client.host, request.method, request.url.path, e, process_time
            )
            # 예외를 다시 발생시켜 FastAPI의 기본 핸들러가 처리하도록 함
            raise