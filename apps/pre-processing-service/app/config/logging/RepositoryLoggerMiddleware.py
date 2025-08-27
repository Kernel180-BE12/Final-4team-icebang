import time
from typing import Dict, Any, List
from fastapi import Request
from loguru import logger
from contextvars import ContextVar

trace_id_context: ContextVar[str] = ContextVar('trace_id', default="NO_TRACE_ID")

class RepositoryLoggingDependency:
    """
    레포지토리 로깅을 위한 의존성 클래스
    :param repository_type: 레포지토리 유형 (예: "VECTOR_DB", "RDB", "REDIS")
    :param track_params: 추적할 매개변수 이름 목록
    """

    def __init__(self, repository_type: str, track_params: List[str] = None):
        self.repository_type = repository_type
        self.track_params = track_params or []

    async def __call__(self, request: Request):
        """
        의존성 주입 시 호출되는 메서드
        :param request: FastAPI Request 객체
        :return: 레포지토리 유형과 추출된 매개변수 딕셔너리
        """
        trace_id = trace_id_context.get("NO_TRACE_ID")
        start_time = time.time()

        # 파라미터 추출
        params = await self._extract_params(request)
        param_str = ""
        if params:
            param_strs = [f"{k}={v}" for k, v in params.items()]
            param_str = " " + " ".join(param_strs)

        logger.info(f"[{self.repository_type}_START] trace_id={trace_id}{param_str}")

        # 응답 시 사용할 정보를 request.state에 저장
        request.state.repository_type = self.repository_type
        request.state.start_time = start_time
        request.state.param_str = param_str

        return {"repository_type": self.repository_type, "params": params}

    async def _extract_params(self, request: Request) -> Dict[str, Any]:
        """
        요청에서 추적 파라미터 추출
        :param request: FastAPI Request 객체
        :return: 추출된 매개변수 딕셔너리
        """
        params = {}

        try:
            # Query Parameters 추출
            for key, value in request.query_params.items():
                if key in self.track_params:
                    params[key] = value

            # JSON Body 추출
            try:
                json_body = await request.json()
                if json_body:
                    for key, value in json_body.items():
                        if key in self.track_params:
                            if isinstance(value, str) and len(value) > 50:
                                params[f"{key}_length"] = len(value)
                            elif isinstance(value, list):
                                params[f"{key}_count"] = len(value)
                            else:
                                params[key] = value
            except:
                pass
        except:
            pass

        return params


# 레포지토리별 의존성 인스턴스 생성
vector_db_dependency = RepositoryLoggingDependency("VECTOR_DB", ["query", "embeddings", "top_k", "collection", "filters"])
rdb_dependency = RepositoryLoggingDependency("RDB", ["table", "where_clause", "limit", "data"])
redis_dependency = RepositoryLoggingDependency("REDIS", ["key", "value", "ttl", "pattern"])
elasticsearch_dependency = RepositoryLoggingDependency("ELASTICSEARCH", ["index", "query", "size", "document"])


# 응답 로깅을 위한 의존성
async def log_repository_response(request: Request):
    """
    레포지토리 응답 시 성공 로그 기록
    :param request: FastAPI Request 객체
    """
    if hasattr(request.state, 'repository_type'):
        trace_id = trace_id_context.get("NO_TRACE_ID")
        duration = time.time() - request.state.start_time
        logger.info(
            f"[{request.state.repository_type}_SUCCESS] trace_id={trace_id} execution_time={duration:.4f}s{request.state.param_str}")
    return None


"""
라우터 예시
@router.post("/search")
async def vector_search(
    query: str,
    top_k: int = 10,
    request: Request = None,
    _: None = Depends(vector_db_dependency),  # 직접 의존성 주입
    __: None = Depends(log_repository_response)
):

또는 라우터 레벨에서:
vector_router = APIRouter(
    prefix="/vector",
    tags=["vector"],
    dependencies=[Depends(vector_db_dependency)]
)
"""