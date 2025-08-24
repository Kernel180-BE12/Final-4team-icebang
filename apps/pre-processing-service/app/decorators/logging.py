# app/decorators/logging.py

from fastapi import Request
from loguru import logger
import functools
import time


def log_api_call(func):
    """
    FastAPI API 호출에 대한 상세 정보를 로깅하는 데코레이터입니다.
    IP 주소, User-Agent, URL, 메서드, 실행 시간 등을 기록합니다.
    """

    @functools.wraps(func)
    async def wrapper(*args, **kwargs):
        # 1. request 객체를 안전하게 가져옵니다.
        #    kwargs에서 'request'를 찾고, 없으면 args가 비어있지 않은 경우에만 args[0]을 시도합니다.
        request: Request | None = kwargs.get('request')
        if request is None and args and isinstance(args[0], Request):
            request = args[0]

        # 2. 로깅에 사용할 추가 정보를 추출합니다.
        client_ip: str | None = None
        user_agent: str | None = None
        if request:
            client_ip = request.client.host
            user_agent = request.headers.get("user-agent", "N/A")

        # 3. 요청 정보를 로그로 기록합니다.
        log_context = {
            "func": func.__name__,
            "ip": client_ip,
            "user_agent": user_agent
        }
        if request:
            log_context.update({
                "url": str(request.url),
                "method": request.method,
            })
            logger.info(
                "API 호출 시작: URL='{url}' 메서드='{method}' 함수='{func}' IP='{ip}' User-Agent='{user_agent}'",
                **log_context
            )
        else:
            logger.info("API 호출 시작: 함수='{func}'", **log_context)

        start_time = time.time()
        result = None

        try:
            # 4. 원본 함수를 실행합니다.
            result = await func(*args, **kwargs)
            return result
        except Exception as e:
            # 5. 예외 발생 시 에러 로그를 기록합니다.
            elapsed_time = time.time() - start_time
            log_context["exception"] = e
            log_context["elapsed"] = f"{elapsed_time:.4f}s"

            if request:
                logger.error(
                    "API 호출 실패: URL='{url}' 메서드='{method}' IP='{ip}' 예외='{exception}' ({elapsed})",
                    **log_context
                )
            else:
                logger.error(
                    "API 호출 실패: 함수='{func}' 예외='{exception}' ({elapsed})",
                    **log_context
                )
            raise  # 예외를 다시 발생시켜 FastAPI가 처리하도록 합니다.
        finally:
            # 6. 성공적으로 완료되면 성공 로그를 기록합니다.
            if result is not None:
                elapsed_time = time.time() - start_time
                log_context["elapsed"] = f"{elapsed_time:.4f}s"
                if request:
                    logger.success(
                        "API 호출 성공: URL='{url}' 메서드='{method}' IP='{ip}' ({elapsed})",
                        **log_context
                    )
                else:
                    logger.success(
                        "API 호출 성공: 함수='{func}' ({elapsed})",
                        **log_context
                    )

    return wrapper