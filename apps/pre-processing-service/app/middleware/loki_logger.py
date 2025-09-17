import json
import aiohttp
import asyncio
from typing import Dict, List, Any, Optional
from datetime import datetime
from loguru import logger

from app.model.execution_log import ExecutionLog
from app.core.config import settings


class LokiLogger:
    """Loki에 로그를 전송하는 클래스"""

    def __init__(self):
        self.loki_url = f"{settings.loki_host}:{settings.loki_port}/loki/api/v1/push"
        self.app_name = settings.app_name
        self.session = None

    async def _get_session(self) -> aiohttp.ClientSession:
        """aiohttp 세션 관리"""
        if self.session is None or self.session.closed:
            self.session = aiohttp.ClientSession()
        return self.session

    async def close(self):
        """세션 종료"""
        if self.session and not self.session.closed:
            await self.session.close()

    async def send_log(
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
        additional_data: Optional[dict] = None
    ) -> bool:
        """
        Loki로 로그 전송

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
            additional_data: 추가 데이터

        Returns:
            bool: 전송 성공 여부
        """
        try:
            execution_log = ExecutionLog(
                execution_type=execution_type,
                source_id=source_id,
                log_level=log_level,
                executed_at=datetime.now(),
                log_message=log_message,
                trace_id=trace_id,
                run_id=run_id,
                status=status,
                duration_ms=duration_ms,
                error_code=error_code,
                reserved4=additional_data
            )

            loki_data = execution_log.to_loki_format(self.app_name)

            # Loki push API 형식으로 변환
            payload = {
                "streams": [
                    {
                        "stream": loki_data["labels"],
                        "values": [
                            [
                                str(loki_data["log"]["timestamp"]),
                                json.dumps(loki_data["log"], ensure_ascii=False)
                            ]
                        ]
                    }
                ]
            }

            session = await self._get_session()

            async with session.post(
                self.loki_url,
                json=payload,
                headers={"Content-Type": "application/json"},
                timeout=aiohttp.ClientTimeout(total=5)
            ) as response:
                if response.status == 204:
                    # logger.debug(f"Loki 로그 전송 성공: {execution_type} - {log_message[:50]}...")
                    return True
                else:
                    response_text = await response.text()
                    logger.error(f"Loki 로그 전송 실패: status={response.status}, response={response_text}")
                    return False

        except asyncio.TimeoutError:
            logger.error("Loki 로그 전송 타임아웃")
            return False
        except Exception as e:
            logger.error(f"Loki 로그 전송 실패: {str(e)}")
            return False

    async def log_start(
        self,
        execution_type: str,
        source_id: int,
        trace_id: str,
        log_message: str,
        run_id: Optional[int] = None,
        additional_data: Optional[dict] = None
    ) -> bool:
        """시작 로그 전송"""
        return await self.send_log(
            execution_type=execution_type,
            source_id=source_id,
            log_level="INFO",
            log_message=log_message,
            trace_id=trace_id,
            run_id=run_id,
            status="RUNNING",
            additional_data=additional_data
        )

    async def log_success(
        self,
        execution_type: str,
        source_id: int,
        trace_id: str,
        log_message: str,
        duration_ms: int,
        run_id: Optional[int] = None,
        additional_data: Optional[dict] = None
    ) -> bool:
        """성공 로그 전송"""
        return await self.send_log(
            execution_type=execution_type,
            source_id=source_id,
            log_level="INFO",
            log_message=log_message,
            trace_id=trace_id,
            run_id=run_id,
            status="SUCCESS",
            duration_ms=duration_ms,
            additional_data=additional_data
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
        additional_data: Optional[dict] = None
    ) -> bool:
        """에러 로그 전송"""
        return await self.send_log(
            execution_type=execution_type,
            source_id=source_id,
            log_level="ERROR",
            log_message=log_message,
            trace_id=trace_id,
            run_id=run_id,
            status="ERROR",
            duration_ms=duration_ms,
            error_code=error_code,
            additional_data=additional_data
        )

    def __del__(self):
        """소멸자에서 세션 정리"""
        if self.session and not self.session.closed:
            try:
                loop = asyncio.get_event_loop()
                if loop.is_running():
                    loop.create_task(self.session.close())
                else:
                    loop.run_until_complete(self.session.close())
            except:
                pass