import functools
import inspect
import time
from typing import Optional, Dict, Any, List
from loguru import logger
from contextvars import ContextVar

# 추적 ID를 저장할 ContextVar
trace_id_context: ContextVar[Optional[str]] = ContextVar('trace_id', default=None)


class ServiceLogger:
    """서비스 레이어 로깅을 관리하는 클래스 애노테이션으로 적용 가능"""

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

    def _log_service(self, service_type: str = "SERVICE", track_params: List[str] = None):
        """
        서비스 레이어 로깅 데코레이터

        Args:
            service_type: 서비스 타입
            track_params: 추적할 파라미터 이름들 (함수 실행시 자동으로 값 추출)
        """

        def decorator(func):
            @functools.wraps(func)
            def wrapper(*args, **kwargs):
                trace_id = self.get_trace_id()
                operation = f"{service_type}_{func.__name__.upper()}"

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

                logger.info(f"[{service_type}_START] trace_id={trace_id} operation={operation}{param_str}")

                try:
                    # 실제 함수 실행
                    result = func(*args, **kwargs)

                    execution_time = time.time() - start_time

                    # 결과 정보 추가
                    result_str = ""
                    if isinstance(result, list):
                        result_str = f" result_count={len(result)}"
                    elif isinstance(result, str):
                        result_str = f" result_length={len(result)}"

                    logger.info(
                        f"[{service_type}_SUCCESS] trace_id={trace_id} operation={operation} execution_time={execution_time:.4f}s{param_str}{result_str}")

                    return result

                except Exception as e:
                    execution_time = time.time() - start_time

                    logger.error(
                        f"[{service_type}_ERROR] trace_id={trace_id} operation={operation} execution_time={execution_time:.4f}s{param_str} error={type(e).__name__}: {str(e)}")

                    raise

            return wrapper

        return decorator

    def log_service_class(self, service_type: str = "SERVICE",
                          method_params: Dict[str, List[str]] = None,
                          exclude_methods: List[str] = None):
        """
        클래스 전체에 로깅을 적용하는 데코레이터

        Args:
            service_type: 서비스 타입
            method_params: 메서드별 추적할 파라미터 {메서드명: [파라미터명들]}
            exclude_methods: 로깅에서 제외할 메서드명들
        """

        def class_decorator(cls):
            exclude_list = exclude_methods or ['__init__', '__new__']

            for name, method in inspect.getmembers(cls, predicate=inspect.isfunction):
                if name.startswith('_') or name in exclude_list:
                    continue

                # 메서드별 파라미터 설정
                track_params = method_params.get(name) if method_params else None

                # 기존 _log_service 데코레이터 적용
                wrapped_method = self._log_service(service_type, track_params)(method)
                setattr(cls, name, wrapped_method)

            return cls

        return class_decorator

    """ 메서드 데코레이터
    @log_service 데코레이터들을 여기에 추가
    service_type : 서비스 이름 (예: CHUNKING)
    track_params : 추적할 파라미터 이름들
    """
    def chunking(self):
        return self._log_service("CHUNKING", ["chunk_size", "overlap", "overlap_ratio", "documents"])

    """ 클래스 데코레이터
    @log_service_class 데코레이터들을 여기에 추가
    service_type : 서비스 이름 (예: CHUNKING)
    method_params : {메서드명: [추적할 파라미터 이름들]}
    exclude_methods : 로깅에서 제외할 메서드명들
    """
    def chunking_class(self):
        return self.log_service_class("CHUNKING", {
            "chunk_text": ["chunk_size", "overlap"]
        })

# 전역 ServiceLogger 싱글톤
service_logger = ServiceLogger()