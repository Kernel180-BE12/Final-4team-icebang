import asyncpg
import os
import threading

from contextlib import asynccontextmanager


class AsyncPostgreSQLManager:
    """
    비동기 PostgreSQL 매니저 클래스 (싱글톤 패턴)
    1. PostgreSQL 데이터베이스 연결 및 관리
    2. 커넥션 풀링 지원
    3. 쿼리 실행 및 결과 반환
    4. 트랜잭션 관리
    5. 애플리케이션 종료 시 전체 풀 종료
    """

    _instance = None
    _lock = threading.Lock()

    def __new__(cls):
        """
        싱글톤 패턴 구현
        스레드 안전성을 위해 Lock 사용
        """

        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = super(AsyncPostgreSQLManager, cls).__new__(cls)
                    cls._instance._initialized = False
        return cls._instance

    def __init__(self):
        """
        PostgreSQL 매니저 초기화
        1. 데이터베이스 연결 설정
        2. 환경 변수에서 데이터베이스 설정 로드
        """

        # 이미 초기화된 경우 재초기화 방지
        if self._initialized:
            return

        self._pool = None
        self._config = {
            'host': os.getenv('DB_HOST', '52.79.235.214'),
            'port': int(os.getenv('DB_PORT', 5432)),
            'database': os.getenv('DB_NAME', 'pre_process'),
            'user': os.getenv('DB_USER', 'postgres'),
            'password': os.getenv('DB_PASSWORD', 'qwer1234')
        }
        self._initialized = True

    @classmethod
    def get_instance(cls):
        """
        싱글톤 인스턴스 반환
        :return: AsyncPostgreSQLManager 인스턴스
        """

        return cls()

    async def init_pool(self, min_size=5, max_size=20):
        """
        비동기 커넥션 풀 초기화
        애플리케이션 시작 시 단 한번만 호출되어야 한다.
        :param min_size: 최소 커넥션 수
        :param max_size: 최대 커넥션 수
        :return: 커넥션 풀 객체
        """

        if self._pool is None:
            self._pool = await asyncpg.create_pool(
                min_size=min_size,
                max_size=max_size,
                **self._config
            )
        return self._pool

    async def _get_connection(self):
        """
        커넥션 풀에서 커넥션 가져오기
        :return: 커넥션 객체
        """

        if self._pool is None:
            await self.init_pool()
        return await self._pool.acquire()

    @asynccontextmanager
    async def get_connection(self):
        """
        커넥션 컨텍스트 매니저
        :return: 커넥션 객체
        """

        if self._pool is None:
            await self.init_pool()
        conn = await self._pool.acquire()
        transaction = None

        try:
            transaction = conn.transaction()
            await transaction.start()
            yield conn
            await transaction.commit()
        except Exception as e:
            if transaction:
                await transaction.rollback()
            raise e
        finally:
            await self._pool.release(conn)

    async def execute_query(self, sql, *params, fetch=False):
        """
        쿼리 실행
        :param sql: 실행할 SQL 쿼리
        :param params: 쿼리 파라미터
        :param fetch: 결과를 가져올지 여부
        :return: 쿼리 결과 (fetch가 True인 경우)
        """

        async with self.get_connection() as conn:
            if fetch:
                return await conn.fetch(sql, *params)
            else:
                return await conn.execute(sql, *params)

    async def fetch_one(self, sql, *params):
        """
        단일 행 조회
        :param sql: SQL 쿼리
        :param params: 쿼리 파라미터
        :return: 단일 행 결과
        """

        async with self.get_connection() as conn:
            return await conn.fetchrow(sql, *params)

    async def fetch_all(self, sql, *params):
        """
        전체 행 조회
        :param sql: SQL 쿼리
        :param params: 쿼리 파라미터
        :return: 전체 행 결과
        """

        async with self.get_connection() as conn:
            return await conn.fetch(sql, *params)

    async def execute(self, sql, *params):
        """
        쿼리 실행 (INSERT, UPDATE, DELETE 등)
        :param sql: SQL 쿼리
        :param params: 쿼리 파라미터
        :return: 실행 결과 상태
        """

        async with self.get_connection() as conn:
            return await conn.execute(sql, *params)

    async def execute_many(self, sql, params_list):
        """
        배치 쿼리 실행
        :param sql: SQL 쿼리
        :param params_list: 파라미터 리스트
        :return: 실행 결과 상태
        """

        async with self.get_connection() as conn:
            return await conn.executemany(sql, params_list)

    async def close_pool(self):
        """
        애플리케이션 종료 시 전체 풀 종료
        :return: None
        """

        if self._pool:
            await self._pool.close()
            self._pool = None
            print("비동기 DB 연결 풀 전체 종료")

"""
# 사용 예시
init_pool() - 애플리케이션 시작 시 단 한번만 호출 (main.py에서 실행, early startup)

"""