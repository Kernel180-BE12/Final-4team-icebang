import os
from loguru import logger
import sys
from contextvars import ContextVar

try:
    from app.middleware.ServiceLoggerMiddleware import trace_id_context
except ImportError:
    trace_id_context: ContextVar[str] = ContextVar("trace_id", default="")


def setup_file_logging():
    """
    PromTail을 통해 Loki로 전송하기 위한 파일 로깅 설정
    """
    # 기존 loguru 핸들러 제거 (기본 콘솔 출력 제거)
    logger.remove()

    log_dir = "/logs/production"

    os.makedirs(log_dir, exist_ok=True)

    log_file_path = os.path.join(log_dir, "app.log")
    error_log_file_path = os.path.join(log_dir, "error.log")

    # trace_id를 포함한 structured 로그 포맷
    def add_trace_id_filter(record):
        try:
            current_trace_id = trace_id_context.get()
            if current_trace_id:
                record["extra"]["trace_id"] = current_trace_id
            else:
                record["extra"]["trace_id"] = ""
        except LookupError:
            record["extra"]["trace_id"] = ""
        return record

    # 파일 로깅에서 LoggingMiddleware 제외하는 필터
    def exclude_logging_middleware_filter(record):
        # LoggingMiddleware의 로그는 파일에 기록하지 않음
        if record["name"] == "app.middleware.logging":
            return False
        return add_trace_id_filter(record)

    structured_format = "{time:YYYY-MM-DD HH:mm:ss.SSS} {level: <8} {thread.name: <15} {name}:{function}:{line} [{extra[trace_id]}] {message}"

    logger.add(
        log_file_path,
        format=structured_format,
        level="DEBUG",
        rotation="100 MB",  # 100MB마다 로테이션
        retention="7 days",  # 7일간 보관
        compression="zip",  # 압축
        enqueue=True,  # 멀티프로세스 안전
        serialize=False,  # JSON 직렬화 비활성화 (PromTail에서 파싱)
        backtrace=True,  # 백트레이스 포함
        diagnose=True,  # 진단 정보 포함
        filter=exclude_logging_middleware_filter,
    )

    # 에러 레벨 이상은 별도 파일에도 기록
    logger.add(
        error_log_file_path,
        format=structured_format,
        level="ERROR",
        rotation="50 MB",
        retention="30 days",
        compression="zip",
        enqueue=True,
        serialize=False,
        backtrace=True,
        diagnose=True,
        filter=exclude_logging_middleware_filter,
    )

    # 개발 환경에서는 콘솔 출력도 유지
    if os.getenv("ENVIRONMENT", "development") == "development":
        logger.add(
            sys.stdout,
            format="[{extra[trace_id]}] {time:YYYY-MM-DD HH:mm:ss} | {level: <8} | {name}:{function}:{line} | {message}",
            level="DEBUG",
            colorize=False,
            filter=add_trace_id_filter,
        )

    logger.info("File logging setup completed for PromTail integration")
    return logger


def get_logger():
    """구성된 로거 인스턴스 반환"""
    return logger
