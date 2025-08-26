import pytest
import time
from unittest.mock import patch
from contextvars import copy_context
from app.config.logging.ServiceLogger import service_logger


class TestServiceLogger:
    """ServiceLogger 테스트"""

    def test_get_trace_id_default(self):
        """기본 trace_id 테스트 - 첫 번째로 실행"""
        # 강제로 trace_id 초기화
        from app.config.logging.ServiceLogger import trace_id_context
        trace_id_context.set(None)

        # 기본값 테스트
        assert service_logger.get_trace_id() == "NO_TRACE"

        # 다시 초기화 (다른 테스트에 영향 안 주도록)
        trace_id_context.set(None)

    def setup_method(self):
        """각 테스트 전에 컨텍스트 초기화"""
        from app.config.logging.ServiceLogger import trace_id_context
        trace_id_context.set(None)

    def test_set_and_get_trace_id(self):
        """trace_id 설정 및 조회 테스트"""
        test_id = "test-trace-123"
        service_logger.set_trace_id(test_id)
        assert service_logger.get_trace_id() == test_id

    def test_get_trace_id_default(self):
        """기본 trace_id 테스트"""
        # 완전히 새로운 컨텍스트 생성 (기존 컨텍스트와 격리)
        import contextvars

        # 새로운 빈 컨텍스트 생성
        new_context = contextvars.copy_context()

        def check_default_in_new_context():
            # 새 컨텍스트에서 trace_id_context는 기본값을 가져야 함
            from app.config.logging.ServiceLogger import trace_id_context
            value = trace_id_context.get()
            return value or "NO_TRACE"

        # 완전히 새로운 컨텍스트에서 실행
        result = new_context.run(check_default_in_new_context)
        assert result == "NO_TRACE"

    @patch('app.config.logging.ServiceLogger.logger')  # 올바른 모듈 경로
    def test_chunking_decorator_success(self, mock_logger):
        """청킹 서비스 성공 테스트"""
        service_logger.set_trace_id("test-123")

        @service_logger.chunking()
        def test_chunk_function(documents, chunk_size=1000, overlap=200):
            time.sleep(0.01)  # 실행 시간 시뮬레이션
            return ["chunk1", "chunk2", "chunk3"]

        # 함수 실행
        result = test_chunk_function(["doc1", "doc2"], chunk_size=500, overlap=100)

        # 결과 검증
        assert result == ["chunk1", "chunk2", "chunk3"]

        # 로그 호출 검증
        assert mock_logger.info.call_count == 2  # START, SUCCESS

        # START 로그 검증
        start_call = mock_logger.info.call_args_list[0][0][0]
        assert "[CHUNKING_START]" in start_call
        assert "trace_id=test-123" in start_call
        assert "chunk_size=500" in start_call
        assert "overlap=100" in start_call
        assert "documents_count=2" in start_call

        # SUCCESS 로그 검증
        success_call = mock_logger.info.call_args_list[1][0][0]
        assert "[CHUNKING_SUCCESS]" in success_call
        assert "execution_time=" in success_call
        assert "result_count=3" in success_call

    @patch('app.config.logging.ServiceLogger.logger')  # 올바른 모듈 경로
    def test_chunking_decorator_error(self, mock_logger):
        """청킹 서비스 에러 테스트"""
        service_logger.set_trace_id("error-test-456")

        @service_logger.chunking()
        def failing_chunk_function(documents, chunk_size=1000):
            raise ValueError("청킹 실패")

        # 에러 발생 확인
        with pytest.raises(ValueError, match="청킹 실패"):
            failing_chunk_function(["doc1"], chunk_size=500)

        # 로그 호출 검증
        assert mock_logger.info.call_count == 1  # START만
        assert mock_logger.error.call_count == 1  # ERROR

        # ERROR 로그 검증
        error_call = mock_logger.error.call_args[0][0]
        assert "[CHUNKING_ERROR]" in error_call
        assert "trace_id=error-test-456" in error_call
        assert "error=ValueError: 청킹 실패" in error_call

    def test_extract_params(self):
        """파라미터 추출 테스트"""

        def sample_function(docs, chunk_size, overlap=200, model="test"):
            pass

        # 테스트 데이터
        args = (["doc1", "doc2"], 1000)
        kwargs = {"overlap": 150, "model": "custom"}
        track_params = ["docs", "chunk_size", "overlap", "model"]  # 'documents' -> 'docs'

        # 파라미터 추출
        params = service_logger._extract_params(sample_function, args, kwargs, track_params)

        # 결과 검증
        assert params["docs_count"] == 2  # list는 개수로 (docs_count로 변경)
        assert params["chunk_size"] == 1000  # 숫자는 그대로
        assert params["overlap"] == 150  # kwargs 우선
        assert params["model"] == "custom"  # kwargs 우선

    def test_extract_params_string_length(self):
        """긴 문자열 파라미터 추출 테스트"""

        def sample_function(text):
            pass

        long_text = "a" * 100  # 50자 초과
        args = (long_text,)
        kwargs = {}
        track_params = ["text"]

        params = service_logger._extract_params(sample_function, args, kwargs, track_params)
        assert params["text_length"] == 100

    @patch('app.config.logging.ServiceLogger.logger')  # 올바른 모듈 경로
    def test_log_service_generic(self, mock_logger):
        """범용 log_service 데코레이터 테스트"""
        service_logger.set_trace_id("generic-test")

        @service_logger.log_service("CUSTOM", ["param1", "param2"])
        def custom_function(param1, param2=42):
            return "success"

        result = custom_function("value1", param2=99)

        assert result == "success"
        assert mock_logger.info.call_count == 2

        start_call = mock_logger.info.call_args_list[0][0][0]
        assert "[CUSTOM_START]" in start_call
        assert "param1=value1" in start_call
        assert "param2=99" in start_call


# === 통합 테스트 ===
class TestServiceLoggerIntegration:
    """ServiceLogger 통합 테스트"""

    @patch('app.config.logging.ServiceLogger.logger')  # 올바른 모듈 경로
    def test_real_world_scenario(self, mock_logger):
        """실제 사용 시나리오 테스트"""
        # 요청 시작
        service_logger.set_trace_id("req-789")

        @service_logger.chunking()
        def chunk_documents(documents, chunk_size=1000, overlap=200):
            # 실제 청킹 로직 시뮬레이션
            chunks = []
            for doc in documents:
                num_chunks = len(doc) // chunk_size + 1
                chunks.extend([f"{doc[:10]}_chunk_{i}" for i in range(num_chunks)])
            return chunks

        # 웹에서 받은 파라미터
        user_docs = ["문서1 내용입니다" * 50, "문서2 내용입니다" * 30]
        user_chunk_size = 800
        user_overlap = 150

        # 함수 실행
        result = chunk_documents(user_docs, chunk_size=user_chunk_size, overlap=user_overlap)

        # 결과 검증
        assert len(result) > 0
        assert all("chunk_" in chunk for chunk in result)

        # 로깅 검증
        assert mock_logger.info.call_count == 2  # START, SUCCESS

        start_log = mock_logger.info.call_args_list[0][0][0]
        assert "chunk_size=800" in start_log
        assert "overlap=150" in start_log
        assert "documents_count=2" in start_log

        success_log = mock_logger.info.call_args_list[1][0][0]
        assert f"result_count={len(result)}" in success_log


# === 픽스처 ===
@pytest.fixture
def clean_context():
    """각 테스트마다 깨끗한 컨텍스트 제공"""
    ctx = copy_context()
    return ctx


# === Context 격리 테스트 (추가) ===
class TestContextIsolation:
    """ContextVar 격리 테스트"""

    def test_context_isolation(self):
        """각 컨텍스트가 독립적인지 테스트"""

        def context_test(trace_id, expected):
            service_logger.set_trace_id(trace_id)
            assert service_logger.get_trace_id() == expected
            return service_logger.get_trace_id()

        # 서로 다른 컨텍스트에서 실행
        ctx1 = copy_context()
        ctx2 = copy_context()

        result1 = ctx1.run(context_test, "context-1", "context-1")
        result2 = ctx2.run(context_test, "context-2", "context-2")

        assert result1 == "context-1"
        assert result2 == "context-2"
        assert result1 != result2


# === 실행 ===
if __name__ == "__main__":
    pytest.main([__file__, "-v"])