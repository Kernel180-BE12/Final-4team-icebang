import pymysql
import os
import threading

from contextlib import contextmanager
from dotenv import load_dotenv
from dbutils.pooled_db import PooledDB


class MariadbManager:
    """
    MariaDB 매니저 클래스
    1. MariaDB 데이터베이스 연결 및 관리
    2. 커넥션 풀링 지원
    3. 커서 및 커넥션 컨텍스트 매니저 제공
    """

    _instance = None
    _lock = threading.Lock()
    load_dotenv()

    def __new__(cls):
        """
        싱글톤 패턴 구현
        스레드 안전성을 위해 Lock 사용
        Double-checked locking 적용
        """

        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = super(MariadbManager, cls).__new__(cls)
                    cls._instance._initialized = False
        return cls._instance

    def __init__(self):
        """
        MariaDB 매니저 초기화
        데이터베이스 연결 설정
        환경 변수에서 데이터베이스 설정 로드 및 검증
        """

        if self._initialized:
            return

        self._config = {
            "host": os.getenv("DB_HOST", "localhost"),
            "port": int(os.getenv("DB_PORT", "3306")),
            "database": os.getenv("DB_NAME", "pre_process"),
            "user": os.getenv("DB_USER", "mariadb"),
            "password": os.getenv("DB_PASSWORD", "qwer1234"),
            "autocommit": False,
        }

        required_keys = ["host", "database", "user", "password"]
        missing = [
            k
            for k, v in self._config.items()
            if k in required_keys and (v is None or v == "")
        ]
        if missing:
            raise ValueError(f"필수 데이터베이스 설정이 누락되었습니다: {missing}")

        self._pool = None
        self._initialized = True

    def _init_pool(self, pool_size=20):
        """
        MariaDB 전용 커넥션 풀 초기화
        :param pool_size: 풀 크기
        """

        if self._pool is None:
            config = {**self._config}
            try:
                self._pool = PooledDB(
                    creator=pymysql,
                    maxconnections=pool_size,
                    mincached=2,
                    maxcached=5,
                    maxshared=3,
                    blocking=True,
                    maxusage=None,
                    setsession=[],
                    ping=0,
                    **config,
                )
            except pymysql.Error as e:
                raise Exception(f"MariaDB 커넥션 풀 초기화 실패: {e}")

    @contextmanager
    def get_cursor(self):
        """
        커서 컨텍스트 매니저 - 일반적인 쿼리용
        :return: 커서 객체
        """

        if self._pool is None:
            self._init_pool()

        try:
            conn = self._pool.connection()
        except Exception as e:
            raise Exception(f"커넥션 풀에서 연결 획득 실패: {e}")

        cursor = None
        try:
            cursor = conn.cursor()
            yield cursor
            conn.commit()
        except Exception as e:
            if conn:
                conn.rollback()
            raise e
        finally:
            if cursor:
                cursor.close()
            if conn:
                conn.close()

    @contextmanager
    def get_connection(self):
        """
        커넥션 컨텍스트 매니저
        :return: 커넥션 객체
        """

        if self._pool is None:
            self._init_pool()

        try:
            conn = self._pool.connection()
        except Exception as e:
            raise Exception(f"커넥션 풀에서 연결 획득 실패: {e}")

        try:
            yield conn
            conn.commit()
        except Exception as e:
            if conn:
                conn.rollback()
            raise e
        finally:
            if conn:
                conn.close()

    def close_pool(self):
        """
        풀 종료
        """
        if self._pool:
            self._pool.close()
            self._pool = None
