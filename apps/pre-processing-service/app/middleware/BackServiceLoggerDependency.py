# import time
# from typing import Dict, Any, List, Optional
# from fastapi import Request
# from loguru import logger
# from contextvars import ContextVar
#
# trace_id_context: ContextVar[str] = ContextVar('trace_id', default="NO_TRACE_ID")
#
#
# class ServiceLoggingDependency:
#     """
#     서비스 로깅을 위한 의존성 클래스
#     :param service_type: 서비스 유형 (예: "CHUNKING", "PARSING", "EMBEDDING")
#     :param track_params: 추적할 매개변수 이름 목록
#     :param response_trackers: 응답에서 추적할 필드 이름 목록 (딕셔너리)
#     """
#
#     def __init__(self, service_type: str,
#                  track_params: List[str] = None,
#                  response_trackers: List[str] = None):
#         self.service_type = service_type
#         self.track_params = track_params or []
#         self.response_trackers = response_trackers or []
#
#     async def __call__(self, request: Request):
#         """
#         의존성 주입 시 호출되는 메서드
#         :param request: FastAPI Request 객체
#         :return: 서비스 유형과 추출된 매개변수 딕셔너리
#         """
#         trace_id = trace_id_context.get("NO_TRACE_ID")
#         start_time = time.time()
#
#         # 파라미터 추출
#         params = await self._extract_params(request)
#         param_str = ""
#         if params:
#             param_strs = [f"{k}={v}" for k, v in params.items()]
#             param_str = " " + " ".join(param_strs)
#
#         logger.info(f"[{self.service_type}_START] trace_id={trace_id}{param_str}")
#
#         # 응답 시 사용할 정보를 request.state에 저장
#         request.state.service_type = self.service_type
#         request.state.start_time = start_time
#         request.state.param_str = param_str
#         request.state.response_trackers = self.response_trackers
#
#         return {"service_type": self.service_type, "params": params}
#
#     async def _extract_params(self, request: Request) -> Dict[str, Any]:
#         """
#         요청에서 추적 파라미터 추출
#         :param request: FastAPI Request 객체
#         :return: 추출된 매개변수 딕셔너리
#         """
#         params = {}
#
#         try:
#             # Query Parameters 추출
#             for key, value in request.query_params.items():
#                 if key in self.track_params:
#                     params[key] = value
#
#             # JSON Body 추출
#             try:
#                 json_body = await request.json()
#                 if json_body:
#                     for key, value in json_body.items():
#                         if key in self.track_params:
#                             if isinstance(value, str) and len(value) > 50:
#                                 params[f"{key}_length"] = len(value)
#                             elif isinstance(value, list):
#                                 params[f"{key}_count"] = len(value)
#                             else:
#                                 params[key] = value
#             except:
#                 pass
#         except:
#             pass
#
#         return params
#
# # 서비스 응답 시 성공 로그 함수
# async def log_service_response_with_data(request: Request, response_data: Optional[Dict] = None):
#     """
#     서비스 응답 시 성공 로그 기록
#     :param request: FastAPI Request 객체
#     :param response_data: 응답 데이터
#     """
#     if hasattr(request.state, 'service_type'):
#         trace_id = trace_id_context.get("NO_TRACE_ID")
#         duration = time.time() - request.state.start_time
#
#         # 기본 로그 문자열
#         log_parts = [f"[{request.state.service_type}_SUCCESS]",
#                      f"trace_id={trace_id}",
#                      f"execution_time={duration:.4f}s{request.state.param_str}"]
#
#         # 응답 데이터에서 추적할 필드 추출
#         if response_data and hasattr(request.state, 'response_trackers'):
#             response_params = []
#             for tracker in request.state.response_trackers:
#                 if tracker in response_data:
#                     value = response_data[tracker]
#                     if isinstance(value, dict):
#                         response_params.append(f"{tracker}_keys={list(value.keys())}")
#                         response_params.append(f"{tracker}_count={len(value)}")
#                     elif isinstance(value, list):
#                         response_params.append(f"{tracker}_count={len(value)}")
#                     else:
#                         response_params.append(f"{tracker}={value}")
#
#             if response_params:
#                 log_parts.append(" ".join(response_params))
#
#         logger.info(" ".join(log_parts))
#     return None
#
# naver_search_dependency = ServiceLoggingDependency(
#     "NAVER_CRAWLING",
#     track_params=["job_id", "schedule_id", "tag", "category", "startDate", "endDate"],
#     response_trackers=["keyword", "total_keyword"]
# )