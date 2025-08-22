# app/decorators/logging.py

from fastapi import Request
from loguru import logger
import functools
import time


def log_api_call(func):
    @functools.wraps(func)
    async def wrapper(*args, **kwargs):
        # 1. request 객체를 안전하게 가져옵니다.
        #    kwargs에서 'request'를 찾고, 없으면 args가 비어있지 않은 경우에만 args[0]을 시도합니다.
        request: Request | None = kwargs.get('request')
        if request is None and args and isinstance(args[0], Request):
            request = args[0]

        # 요청 정보를 로그로 기록 (request 객체가 있는 경우에만)
        if request:
            logger.info(
                "API 호출 시작: URL='{}' 메서드='{}' 함수='{}'",
                request.url, request.method, func.__name__
            )
        else:
            logger.info("API 호출 시작: 함수='{}'", func.__name__)

        start_time = time.time()
        result = None

        try:
            result = await func(*args, **kwargs)
            return result
        except Exception as e:
            elapsed_time = time.time() - start_time
            if request:
                logger.error(
                    "API 호출 실패: URL='{}' 메서드='{}' 예외='{}' ({:.4f}s)",
                    request.url, request.method, e, elapsed_time
                )
            else:
                logger.error("API 호출 실패: 함수='{}' 예외='{}' ({:.4f}s)", func.__name__, e, elapsed_time)
            raise
        finally:
            if result is not None:
                elapsed_time = time.time() - start_time
                if request:
                    logger.success(
                        "API 호출 성공: URL='{}' 메서드='{}' ({:.4f}s)",
                        request.url, request.method, elapsed_time
                    )
                else:
                    logger.success("API 호출 성공: 함수='{}' ({:.4f}s)", func.__name__, elapsed_time)

    return wrapper