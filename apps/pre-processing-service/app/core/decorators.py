from fastapi import FastAPI, Request
from loguru import logger
import functools
import time


# 2. 로그를 위한 커스텀 데코레이터 정의
def log_api_call(func):
    """
    FastAPI 라우트 함수에 대한 로깅을 수행하는 데코레이터입니다.
    함수 호출 시간, 인자, 반환값, 발생한 예외 등을 기록합니다.
    """

    @functools.wraps(func)
    async def wrapper(*args, **kwargs):
        # 데코레이터가 적용된 함수에 대한 정보를 가져옵니다.
        # FastAPI 라우트 핸들러의 경우, 첫 번째 인자는 Request 객체입니다.
        request: Request = kwargs.get('request', None) or args[0]

        # 함수 실행 전 로그 기록
        logger.info(
            "API 호출 시작: URL='{}' 메서드='{}' 함수='{}'",
            request.url, request.method, func.__name__
        )

        start_time = time.time()
        result = None

        try:
            # 원래 함수 실행
            result = await func(*args, **kwargs)
            return result

        except Exception as e:
            # 예외 발생 시 로그 기록
            elapsed_time = time.time() - start_time
            logger.error(
                "API 호출 실패: URL='{}' 메서드='{}' 함수='{}' 예외='{}' ({:.4f}s)",
                request.url, request.method, func.__name__, e, elapsed_time
            )
            # 예외를 다시 발생시켜 FastAPI의 기본 예외 핸들러가 처리하도록 함
            raise

        finally:
            # 함수 실행 완료(성공 또는 실패) 후 로그 기록
            if result is not None:
                elapsed_time = time.time() - start_time
                logger.success(
                    "API 호출 성공: URL='{}' 메서드='{}' 함수='{}' 반환값='{}' ({:.4f}s)",
                    request.url, request.method, func.__name__, result, elapsed_time
                )

    return wrapper