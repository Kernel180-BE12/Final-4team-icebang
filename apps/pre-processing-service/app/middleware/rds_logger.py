from typing import Optional
from datetime import datetime
import traceback
from loguru import logger

from app.db.mariadb_manager import MariadbManager
from app.model.execution_log import ExecutionLog


class RDSLogger:
    """RDS(MariaDB)에 로그를 저장하는 클래스"""

    def __init__(self):
        self.db_manager = MariadbManager()
        self.max_log_message_length = 450

    def _truncate_log_message(self, log_message: str) -> str:
        """
        log_message를 VARCHAR(500) 크기에 맞게 자르기

        Args:
            log_message: 원본 로그 메시지

        Returns:
            str: 잘린 로그 메시지
        """
        if not log_message:
            return log_message or ""

        # UTF-8 인코딩 기준으로 바이트 길이 체크
        encoded_message = log_message.encode("utf-8")
        original_length = len(encoded_message)

        if original_length <= self.max_log_message_length:
            return log_message

        # 메시지가 너무 길면 자르기
        truncate_suffix = "... [TRUNCATED]"
        available_length = self.max_log_message_length - len(
            truncate_suffix.encode("utf-8")
        )

        truncated_message = encoded_message[:available_length].decode(
            "utf-8", errors="ignore"
        )
        truncated_message += truncate_suffix

        logger.warning(
            f"로그 메시지 잘림: {original_length} bytes -> {len(truncated_message.encode('utf-8'))} bytes"
        )
        return truncated_message

    async def log_execution(
        self,
        execution_type: str,
        source_id: int,
        log_level: str,
        log_message: str,
        trace_id: Optional[str] = None,
        run_id: Optional[int] = None,
        status: Optional[str] = None,
        duration_ms: Optional[int] = None,
        error_code: Optional[str] = None,
        additional_data: Optional[dict] = None,
    ) -> bool:
        """
        execution_log 테이블에 로그 저장

        Args:
            execution_type: task, schedule, job, workflow
            source_id: 모든 데이터에 대한 ID
            log_level: INFO, ERROR, WARNING, DEBUG
            log_message: 로그 메시지
            trace_id: 추적 ID
            run_id: 실행 ID
            status: SUCCESS, ERROR, RUNNING, PENDING
            duration_ms: 실행 시간(밀리초)
            error_code: 에러 코드
            additional_data: 추가 데이터 (reserved4에 JSON으로 저장)

        Returns:
            bool: 저장 성공 여부
        """
        try:
            # TODO: Issue #XXX - log_message VARCHAR(500) 제한으로 인한 임시 truncation
            truncated_log_message = self._truncate_log_message(log_message)
            # 향후 TEXT 타입으로 변경하거나 별도 로그 저장소 검토 필요
            execution_log = ExecutionLog(
                execution_type=execution_type,
                source_id=source_id,
                log_level=log_level,
                executed_at=datetime.now(),
                log_message=truncated_log_message,
                trace_id=trace_id,
                run_id=run_id,
                status=status,
                duration_ms=duration_ms,
                error_code=error_code,
                reserved4=additional_data,
            )

            log_data = execution_log.to_dict()

            # 컬럼명과 값 분리
            columns = list(log_data.keys())
            values = list(log_data.values())
            placeholders = ", ".join(["%s"] * len(values))
            columns_str = ", ".join(columns)

            insert_query = f"""
            INSERT INTO execution_log ({columns_str})
            VALUES ({placeholders})
            """

            with self.db_manager.get_cursor() as cursor:
                cursor.execute(insert_query, values)

            # logger.debug(f"RDS 로그 저장 성공: {execution_type} - {log_message[:50]}...")
            return True

        except Exception as e:
            logger.error(f"RDS 로그 저장 실패: {str(e)}")
            logger.error(f"Traceback: {traceback.format_exc()}")
            return False

    async def log_start(
        self,
        execution_type: str,
        source_id: int,
        trace_id: str,
        log_message: str,
        run_id: Optional[int] = None,
        additional_data: Optional[dict] = None,
    ) -> bool:
        """시작 로그 저장"""
        return await self.log_execution(
            execution_type=execution_type,
            source_id=source_id,
            log_level="INFO",
            log_message=log_message,
            trace_id=trace_id,
            run_id=run_id,
            status="RUNNING",
            additional_data=additional_data,
        )

    async def log_success(
        self,
        execution_type: str,
        source_id: int,
        trace_id: str,
        log_message: str,
        duration_ms: int,
        run_id: Optional[int] = None,
        additional_data: Optional[dict] = None,
    ) -> bool:
        """성공 로그 저장"""
        return await self.log_execution(
            execution_type=execution_type,
            source_id=source_id,
            log_level="INFO",
            log_message=log_message,
            trace_id=trace_id,
            run_id=run_id,
            status="SUCCESS",
            duration_ms=duration_ms,
            additional_data=additional_data,
        )

    async def log_error(
        self,
        execution_type: str,
        source_id: int,
        trace_id: str,
        log_message: str,
        error_code: str,
        duration_ms: Optional[int] = None,
        run_id: Optional[int] = None,
        additional_data: Optional[dict] = None,
    ) -> bool:
        """에러 로그 저장"""
        return await self.log_execution(
            execution_type=execution_type,
            source_id=source_id,
            log_level="ERROR",
            log_message=log_message,
            trace_id=trace_id,
            run_id=run_id,
            status="ERROR",
            duration_ms=duration_ms,
            error_code=error_code,
            additional_data=additional_data,
        )
