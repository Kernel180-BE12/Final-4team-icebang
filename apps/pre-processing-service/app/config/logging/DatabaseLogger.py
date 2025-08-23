import functools
import inspect
import time
from typing import Optional, Dict, Any, List
from loguru import logger
from contextvars import ContextVar

# 추적 ID를 저장할 ContextVar
trace_id_context: ContextVar[Optional[str]] = ContextVar('trace_id', default=None)


class DatabaseLogger:
    """통합 데이터베이스 로깅을 관리하는 클래스 (벡터DB + RDB)"""

    def set_trace_id(self, trace_id: str):
        """스프링에서 받은 추적 ID 설정"""
        trace_id_context.set(trace_id)

    def get_trace_id(self) -> str:
        """현재 추적 ID 반환"""
        return trace_id_context.get() or "NO_TRACE"

    def _extract_params(self, func, args, kwargs, param_names: List[str]) -> Dict[str, Any]:
        """함수의 실제 파라미터 값들을 추출"""
        params = {}

        # 함수의 파라미터 이름들 가져오기
        sig = inspect.signature(func)
        param_list = list(sig.parameters.keys())

        # kwargs에서 직접 찾기
        for param_name in param_names:
            if param_name in kwargs:
                params[param_name] = kwargs[param_name]

        # args에서 찾기
        for i, arg_value in enumerate(args):
            if i < len(param_list):
                param_name = param_list[i]
                if param_name in param_names and param_name not in params:
                    if isinstance(arg_value, list):
                        params[f"{param_name}_count"] = len(arg_value)
                    elif isinstance(arg_value, str) and len(arg_value) > 50:
                        params[f"{param_name}_length"] = len(arg_value)
                    else:
                        params[param_name] = arg_value

        return params

    def _format_result_info(self, result: Any) -> str:
        """결과 정보를 포맷팅"""
        if isinstance(result, list):
            return f" result_count={len(result)}"
        elif isinstance(result, dict):
            return f" result_keys={len(result.keys())}"
        elif isinstance(result, str):
            return f" result_length={len(result)}"
        elif hasattr(result, '__len__'):
            return f" result_count={len(result)}"
        else:
            return ""

    def _log_db_operation(self, db_type: str, operation: str, track_params: List[str] = None):
        """
        데이터베이스 작업 로깅 데코레이터의 핵심 로직

        Args:
            db_type: 데이터베이스 타입 (VECTOR_DB, RDB)
            operation: 작업 타입
            track_params: 추적할 파라미터 이름들
        """

        def decorator(func):
            @functools.wraps(func)
            def wrapper(*args, **kwargs):
                trace_id = self.get_trace_id()
                full_operation = f"{db_type}_{operation}_{func.__name__.upper()}"

                # 동적으로 파라미터 추출
                tracked_params = {}
                if track_params:
                    tracked_params = self._extract_params(func, args, kwargs, track_params)

                # 파라미터를 문자열로 변환
                param_str = ""
                if tracked_params:
                    param_strs = [f"{k}={v}" for k, v in tracked_params.items()]
                    param_str = " " + " ".join(param_strs)

                start_time = time.time()

                # 작업 시작 로그
                logger.info(f"[{db_type}_START] trace_id={trace_id} operation={full_operation}{param_str}")

                try:
                    # 실제 함수 실행
                    result = func(*args, **kwargs)

                    # 실행 시간 계산
                    execution_time = time.time() - start_time

                    # 결과 정보
                    result_str = self._format_result_info(result)

                    # 성공 로그
                    logger.info(
                        f"[{db_type}_SUCCESS] trace_id={trace_id} operation={full_operation} execution_time={execution_time:.4f}s{param_str}{result_str}")

                    return result

                except Exception as e:
                    # 실행 시간 계산
                    execution_time = time.time() - start_time

                    # 실패 로그
                    logger.error(
                        f"[{db_type}_ERROR] trace_id={trace_id} operation={full_operation} execution_time={execution_time:.4f}s{param_str} error={type(e).__name__}: {str(e)}")

                    raise

            return wrapper

        return decorator

    def custom_db_operation(self, db_type: str, operation: str, track_params: List[str] = None):
        """커스텀 데이터베이스 작업 로깅 - 사용자 정의 용"""
        return self._log_db_operation(db_type, operation, track_params)


# 전역 DatabaseLogger 싱글톤
db_logger = DatabaseLogger()