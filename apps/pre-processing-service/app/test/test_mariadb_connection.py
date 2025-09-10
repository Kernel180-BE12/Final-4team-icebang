import pytest
import threading
from dotenv import load_dotenv

from app.db.mariadb_manager import MariadbManager


class TestMariadbManager:
    """
    MariaDB Manager 테스트
    1. 싱글톤 패턴 확인
    2. 환경변수 로드 테스트
    3. 커넥션풀 초기화 테스트
    4. 커서 컨텍스트 매니저 및 SELECT 1 테스트
    5. 커넥션 컨텍스트 매니저 및 SELECT 1 테스트
    """

    def setup_method(self):
        """각 테스트 메서드 실행 전 초기화"""

        MariadbManager._instance = None
        if hasattr(MariadbManager, '_initialized'):
            MariadbManager._initialized = False

    def teardown_method(self):
        """각 테스트 메서드 실행 후 정리"""

        if MariadbManager._instance and hasattr(MariadbManager._instance, '_pool'):
            if MariadbManager._instance._pool:
                MariadbManager._instance.close_pool()
        MariadbManager._instance = None

    def test_singleton_pattern(self):
        """싱글톤 패턴 확인 테스트"""

        manager1 = MariadbManager()
        manager2 = MariadbManager()

        assert manager1 is manager2, "싱글톤 패턴이 제대로 작동하지 않습니다"
        assert id(manager1) == id(manager2), "인스턴스 ID가 다릅니다"

        instances = []

        def create_instance():
            instance = MariadbManager()
            instances.append(instance)

        threads = []
        for i in range(5):
            thread = threading.Thread(target=create_instance, name=f"Thread-{i}")
            threads.append(thread)
            thread.start()

        for thread in threads:
            thread.join()

        for i, instance in enumerate(instances):
            assert instance is manager1, f"스레드 {i}에서 생성된 인스턴스가 다릅니다"

    def test_environment_variables_load(self):
        """환경변수 로드 테스트"""

        manager = MariadbManager()
        config = manager._config

        required_keys = ['host', 'port', 'database', 'user', 'password']
        for key in required_keys:
            assert key in config, f"필수 설정 {key}가 누락되었습니다"
            assert config[key] is not None, f"설정 {key}의 값이 None입니다"
            if isinstance(config[key], str):
                assert config[key].strip() != '', f"설정 {key}의 값이 비어있습니다"

        assert isinstance(config['port'], int), "포트는 정수여야 합니다"
        assert config['port'] > 0, "포트는 양수여야 합니다"

    def test_connection_pool_initialization(self):
        """커넥션풀 초기화 테스트"""

        manager = MariadbManager()

        assert manager._pool is None, "초기 풀 상태가 None이 아닙니다"

        try:
            manager._init_pool(pool_size=5)
        except Exception as e:
            pytest.fail(f"커넥션풀 초기화 실패: {e}")

        assert manager._pool is not None, "풀이 생성되지 않았습니다"

        try:
            conn = manager._pool.connection()
            conn.close()
        except Exception as e:
            pytest.fail(f"풀에서 연결 획득 실패: {e}")

    def test_cursor_context_manager_with_select1(self):
        """커서 컨텍스트 매니저 및 SELECT 1 테스트"""

        manager = MariadbManager()

        try:
            with manager.get_cursor() as cursor:
                cursor.execute("SELECT 1")
                result = cursor.fetchone()

                assert result is not None, "SELECT 1 결과가 None입니다"
                assert result[0] == 1, f"SELECT 1 결과가 1이 아닙니다: {result[0]}"

                cursor.execute("SELECT NOW()")
                time_result = cursor.fetchone()
                assert time_result is not None, "NOW() 결과가 None입니다"

                cursor.execute("SELECT VERSION()")
                version_result = cursor.fetchone()
                assert version_result is not None, "VERSION() 결과가 None입니다"

        except Exception as e:
            pytest.fail(f"커서 컨텍스트 매니저 테스트 실패: {e}")

    def test_connection_context_manager_with_select1(self):
        """커넥션 컨텍스트 매니저 및 SELECT 1 테스트"""

        manager = MariadbManager()

        try:
            with manager.get_connection() as conn:
                cursor = conn.cursor()

                try:
                    cursor.execute("SELECT 1")
                    result = cursor.fetchone()

                    assert result is not None, "SELECT 1 결과가 None입니다"
                    assert result[0] == 1, f"SELECT 1 결과가 1이 아닙니다: {result[0]}"

                    cursor.execute("SELECT CONNECTION_ID()")
                    conn_info = cursor.fetchone()
                    assert conn_info is not None, "CONNECTION_ID() 결과가 None입니다"

                    cursor.execute("SELECT USER()")
                    user_info = cursor.fetchone()
                    assert user_info is not None, "USER() 결과가 None입니다"

                finally:
                    cursor.close()

        except Exception as e:
            pytest.fail(f"커넥션 컨텍스트 매니저 테스트 실패: {e}")
