from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import Response
from loguru import logger
from contextvars import ContextVar
from fastapi.responses import JSONResponse
from typing import Dict, Optional, List, Any

import json
import time

trace_id_context: ContextVar[str] = ContextVar('trace_id', default="NO_TRACE_ID")


class ServiceLoggerMiddleware(BaseHTTPMiddleware):
    """
    완전 자동 서비스 로깅 미들웨어 - 의존성 주입 불필요
    URL 패턴을 기반으로 자동으로 서비스 타입 식별 및 로깅
    """

    def __init__(self, app, service_mappings: Dict[str, Dict] = None):
        """
        :param service_mappings: URL 패턴별 서비스 설정
        예: {
            "/keywords/search": {
                "service_type": "NAVER_CRAWLING",
                "track_params": ["keyword", "category"],
                "response_trackers": ["total_keywords", "results_count"]
            }
        }
        """
        super().__init__(app)
        self.service_mappings = service_mappings or self._default_mappings()

    def _default_mappings(self) -> Dict[str, Dict]:
        """기본 서비스 매핑 설정"""
        return {
            "/keywords/search": {
                "service_type": "NAVER_CRAWLING",
                "track_params": ["keyword", "category", "startDate", "endDate", "job_id", "schedule_id"],
                "response_trackers": ["keyword", "total_keywords", "results_count"]
            }
        }

    async def dispatch(self, request: Request, call_next):
        """요청-응답 사이클을 가로채서 자동 로깅 처리"""

        # 1. 서비스 설정 확인
        service_config = self._get_service_config(request.url.path)
        if not service_config:
            # 로깅 대상이 아닌 경우 그냥 통과
            return await call_next(request)

        # 2. 시작 로깅
        trace_id = trace_id_context.get("NO_TRACE_ID")
        start_time = time.time()

        # 파라미터 추출 및 시작 로그
        params = await self._extract_params(request, service_config["track_params"])
        param_str = ""
        if params:
            param_strs = [f"{k}={v}" for k, v in params.items()]
            param_str = " " + " ".join(param_strs)

        service_type = service_config["service_type"]
        logger.info(f"[{service_type}_START] trace_id={trace_id}{param_str}")

        # 3. 요청 처리
        try:
            response = await call_next(request)

            # 4. 성공 로깅
            if 200 <= response.status_code < 300:
                await self._log_success_response(
                    service_type, trace_id, start_time, param_str,
                    response, service_config["response_trackers"]
                )
            else:
                await self._log_error_response(
                    service_type, trace_id, start_time, param_str, response
                )

            return response

        except Exception as e:
            # 5. 예외 로깅
            await self._log_exception(service_type, trace_id, start_time, param_str, e)
            raise

    def _get_service_config(self, url_path: str) -> Optional[Dict]:
        """URL 경로를 기반으로 서비스 설정 반환"""
        for pattern, config in self.service_mappings.items():
            if self._match_pattern(url_path, pattern):
                return config
        return None

    def _match_pattern(self, url_path: str, pattern: str) -> bool:
        """URL 패턴 매칭 (간단한 구현, 필요시 정규식으로 확장 가능)"""
        # 정확히 일치하거나 패턴이 접두사인 경우
        return url_path == pattern or url_path.startswith(pattern.rstrip('*'))

    async def _extract_params(self, request: Request, track_params: List[str]) -> Dict[str, Any]:
        """요청에서 추적 파라미터 추출"""
        params = {}

        try:
            # Query Parameters 추출
            for key, value in request.query_params.items():
                if key in track_params:
                    params[key] = value

            # JSON Body 추출
            try:
                # request body를 읽기 위한 안전한 방법
                body = await request.body()
                if body:
                    json_body = json.loads(body.decode())
                    if isinstance(json_body, dict):
                        for key, value in json_body.items():
                            if key in track_params:
                                if isinstance(value, str) and len(value) > 50:
                                    params[f"{key}_length"] = len(value)
                                elif isinstance(value, list):
                                    params[f"{key}_count"] = len(value)
                                else:
                                    params[key] = value
            except:
                pass

        except Exception as e:
            logger.debug(f"파라미터 추출 실패: {e}")

        return params

    async def _log_success_response(self, service_type: str, trace_id: str,
                                    start_time: float, param_str: str,
                                    response: Response, response_trackers: List[str]):
        """성공 응답 로깅"""
        duration = time.time() - start_time

        log_parts = [
            f"[{service_type}_SUCCESS]",
            f"trace_id={trace_id}",
            f"execution_time={duration:.4f}s{param_str}",
            f"status_code={response.status_code}"
        ]

        # 응답 데이터에서 추적 정보 추출
        if isinstance(response, JSONResponse) and response_trackers:
            try:
                # JSONResponse body 읽기
                if hasattr(response, 'body'):
                    response_data = json.loads(response.body.decode())
                elif hasattr(response, 'content'):
                    response_data = response.content
                else:
                    response_data = None

                if response_data and isinstance(response_data, dict):
                    response_params = []
                    for tracker in response_trackers:
                        if tracker in response_data:
                            value = response_data[tracker]
                            if isinstance(value, dict):
                                response_params.append(f"{tracker}_keys={list(value.keys())}")
                                response_params.append(f"{tracker}_count={len(value)}")
                            elif isinstance(value, list):
                                response_params.append(f"{tracker}_count={len(value)}")
                            else:
                                response_params.append(f"{tracker}={value}")

                    if response_params:
                        log_parts.append(" ".join(response_params))

            except Exception as e:
                logger.debug(f"응답 추적 정보 추출 실패: {e}")

        logger.info(" ".join(log_parts))

    async def _log_error_response(self, service_type: str, trace_id: str,
                                  start_time: float, param_str: str, response: Response):
        """에러 응답 로깅"""
        duration = time.time() - start_time
        logger.error(
            f"[{service_type}_ERROR] trace_id={trace_id} "
            f"execution_time={duration:.4f}s{param_str} "
            f"status_code={response.status_code}"
        )

    async def _log_exception(self, service_type: str, trace_id: str,
                             start_time: float, param_str: str, exception: Exception):
        """예외 로깅"""
        duration = time.time() - start_time
        logger.error(
            f"[{service_type}_EXCEPTION] trace_id={trace_id} "
            f"execution_time={duration:.4f}s{param_str} "
            f"exception={str(exception)}"
        )