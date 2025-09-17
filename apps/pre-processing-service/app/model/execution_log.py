from dataclasses import dataclass
from datetime import datetime
from typing import Optional, Dict, Any
import json


@dataclass
class ExecutionLog:
    """execution_log 테이블에 대응하는 데이터 모델"""

    execution_type: str  # task, schedule, job, workflow
    source_id: int  # 모든 데이터에 대한 ID
    log_level: str  # INFO, ERROR, WARNING, DEBUG
    executed_at: datetime
    log_message: str
    span_id: str =""#테스트값
    trace_id: Optional[str] = None
    run_id: Optional[int] = None
    status: Optional[str] = None  # SUCCESS, ERROR, RUNNING, PENDING
    duration_ms: Optional[int] = None
    error_code: Optional[str] = None
    reserved1: Optional[str] = None
    reserved2: Optional[str] = None
    reserved3: Optional[int] = None
    reserved4: Optional[Dict[str, Any]] = None  # JSON 데이터
    reserved5: Optional[datetime] = None
    id: Optional[int] = None  # auto_increment

    def to_dict(self) -> Dict[str, Any]:
        """딕셔너리로 변환 (DB 삽입용)"""
        data = {
            'execution_type': self.execution_type,
            'source_id': self.source_id,
            'log_level': self.log_level,
            'executed_at': self.executed_at,
            'log_message': self.log_message,
            'trace_id': self.trace_id,
            'run_id': self.run_id,
            'status': self.status,
            'duration_ms': self.duration_ms,
            'error_code': self.error_code,
            'reserved1': self.span_id,
            'reserved2': self.reserved2,
            'reserved3': self.reserved3,
            'reserved4': json.dumps(self.reserved4) if self.reserved4 else None,
            'reserved5': self.reserved5
        }
        return {k: v for k, v in data.items() if v is not None}

    def to_loki_format(self, app_name: str = "pre-processing-service") -> Dict[str, Any]:
        """Loki 형식으로 변환"""

        labels = {
            "app": app_name,
            "env": "develop",
            "traceId": self.trace_id or "NO_TRACE_ID",
            "spanId": self.span_id,  # 필요시 추가
            "executionType": self.execution_type,
            "sourceId": str(self.source_id),
            "runId": str(self.run_id) if self.run_id else ""
        }

        log_data = {
            "timestamp": int(self.executed_at.timestamp() * 1000000000),  # nanoseconds
            "level": self.log_level,
            "message": self.log_message,
            "execution_type": self.execution_type,
            "source_id": self.source_id,
            "status": self.status,
            "duration_ms": self.duration_ms,
            "error_code": self.error_code
        }

        if self.reserved4:
            log_data.update(self.reserved4)

        return {
            "labels": labels,
            "log": log_data
        }