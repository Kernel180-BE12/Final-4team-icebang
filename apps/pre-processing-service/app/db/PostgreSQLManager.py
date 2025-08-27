from contextlib import contextmanager

import psycopg2
import psycopg2.pool
import os

class PostgreSQLManager:
    """
    PostgreSQL 매니저 클래스
    1. PostgreSQL 데이터베이스 연결 및 관리
    2. 커넥션 풀링 지원
    3. 쿼리 실행 및 결과 반환
    4. 트랜잭션 관리
    5. 애플리케이션 종료 시 전체 풀 종료
    """

    def __init__(self):
        """
        PostgreSQL 매니저 초기화
        1. 데이터베이스 연결 설정
        2. 환경 변수에서 데이터베이스 설정 로드

        """
        self._pool = None
        self._config = {
            'host': os.getenv('DB_HOST', '52.79.235.214'),
            'port': os.getenv('DB_PORT', '5432'),
            'database': os.getenv('DB_NAME', 'pre_process'),
            'user': os.getenv('DB_USER', 'postgres'),
            'password': os.getenv('DB_PASSWORD', 'qwer1234')
        }

    def _init_pool(self, min_conn=5, max_conn=20, **custom_config):
        """
        커넥션 풀 초기화
        :param min_conn: 최소 커넥션 수
        :param max_conn: 최대 커넥션 수
        :param custom_config: 커스텀 데이터베이스 설정
        :return: None
        """

        if self._pool is None:

            self._pool = psycopg2.pool.ThreadedConnectionPool(
                min_conn, max_conn, **self._config
            )

    def get_connection(self):
        """
        커넥션 풀에서 커넥션 가져오기
        :return: 커넥션 객체
        """

        if self._pool is None:
            self._init_pool()
        return self._pool.getconn()

    @contextmanager
    def get_cursor(self):
        """
        커서 컨텍스트 매니저
        :return:
        """
        conn = self.get_connection()
        cursor = None
        try:
            cursor = conn.cursor()
            yield cursor
            conn.commit()
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            if cursor:
                cursor.close()
            self._pool.putconn(conn)

    def execute_query(self, sql, params=None, fetch=False):
        """
        쿼리 실행
        :param sql: 실행할 SQL 쿼리
        :param params: 쿼리 파라미터
        :param fetch: 결과를 가져올지 여부
        :return: 쿼리 결과 (fetch가 True인 경우)
        """
        with self.get_cursor() as cursor:
            cursor.execute(sql, params)
            if fetch:
                return cursor.fetchall()

    def close_pool(self):
        """
        애플리케이션 종료 시 전체 풀 종료
        :return: None
        """
        if self._pool:
            self._pool.closeall()
            self._pool = None
            print("DB 연결 풀 전체 종료")

# 싱글톤 패턴으로 PostgreSQLManager 인스턴스 관리
_db_instance = None
def get_db():
    global _db_instance
    if _db_instance is None:
        _db_instance = PostgreSQLManager()
    return _db_instance