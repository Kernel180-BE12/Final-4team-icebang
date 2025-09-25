# pydantic_settings에서 SettingsConfigDict를 추가로 import 합니다.
from pydantic_settings import BaseSettings, SettingsConfigDict
import os
import platform
import subprocess
from typing import Optional


def detect_mecab_dicdir() -> Optional[str]:
    """MeCab 사전 경로 자동 감지"""

    # 1. mecab-config 명령어로 사전 경로 확인 (가장 정확한 방법)
    try:
        result = subprocess.run(
            ["mecab-config", "--dicdir"], capture_output=True, text=True, timeout=5
        )
        if result.returncode == 0:
            dicdir = result.stdout.strip()
            if os.path.exists(dicdir):
                print(f"mecab-config에서 사전 경로 발견: {dicdir}")
                return dicdir
    except (
        subprocess.CalledProcessError,
        FileNotFoundError,
        subprocess.TimeoutExpired,
    ):
        pass

    # 2. 플랫폼별 일반적인 경로들 확인
    system = platform.system().lower()

    if system == "darwin":  # macOS
        candidate_paths = [
            "/opt/homebrew/lib/mecab/dic/mecab-ko-dic",  # Apple Silicon
            "/usr/local/lib/mecab/dic/mecab-ko-dic",  # Intel Mac
            "/opt/homebrew/lib/mecab/dic/mecab-ipadic",  # 기본 사전
            "/usr/local/lib/mecab/dic/mecab-ipadic",
        ]
    elif system == "linux":
        candidate_paths = [
            "/usr/lib/x86_64-linux-gnu/mecab/dic/mecab-ko-dic",
            "/usr/lib/mecab/dic/mecab-ko-dic",
            "/usr/local/lib/mecab/dic/mecab-ko-dic",
            "/usr/share/mecab/dic/mecab-ko-dic",
            "/usr/lib/mecab/dic/mecab-ipadic",
            "/usr/local/lib/mecab/dic/mecab-ipadic",
        ]
    elif system == "windows":
        candidate_paths = [
            "C:/Program Files/MeCab/dic/mecab-ko-dic",
            "C:/mecab/dic/mecab-ko-dic",
            "C:/Program Files/MeCab/dic/mecab-ipadic",
        ]
    else:
        candidate_paths = []

    # 경로 존재 여부 확인
    for path in candidate_paths:
        if os.path.exists(path):
            # dicrc 파일 존재 확인 (실제 사전인지 검증)
            dicrc_path = os.path.join(path, "dicrc")
            if os.path.exists(dicrc_path):
                print(f"플랫폼 기본 경로에서 사전 발견: {path}")
                return path

    return None


# 공통 설정을 위한 BaseSettings
class BaseSettingsConfig(BaseSettings):

    # db_url 대신 개별 필드를 정의합니다.
    db_host: str
    db_port: str
    db_user: str
    db_pass: str
    db_name: str
    env_name: str
    app_name: str

    # MeCab 사전 경로 (자동 감지)
    mecab_path: Optional[str] = None

    # Loki 설정
    loki_host: str
    loki_username: str
    loki_password: str
    loki_port: int = 3100

    grafana_cloud_prometheus_url: Optional[str] = None
    grafana_cloud_prometheus_user: Optional[str] = None
    grafana_cloud_api_key: Optional[str] = None

    # S3 업로드 관련 설정
    AWS_ACCESS_KEY_ID: str = None
    AWS_SECRET_ACCESS_KEY: str = None
    S3_BUCKET_NAME: str = None
    AWS_REGION: str = None

    # S3 업로드 옵션 설정
    S3_BASE_FOLDER: str = None
    S3_UPLOAD_ENABLED: bool = True
    IMAGE_DOWNLOAD_TIMEOUT: int = 30
    MAX_IMAGE_SIZE_MB: int = 10

    # 테스트/추가용 필드
    OPENAI_API_KEY: Optional[str] = None  # << 이 부분 추가

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

        # mecab_path가 설정되지 않았으면 자동 감지
        if not self.mecab_path:
            self.mecab_path = detect_mecab_dicdir()
            if not self.mecab_path:
                print("MeCab 사전 경로를 찾을 수 없어 기본 설정으로 실행합니다.")

    @property
    def db_url(self) -> str:
        """개별 필드를 사용하여 DB URL을 동적으로 생성"""
        return (
            f"mysql+pymysql://{self.db_user}:"
            f"{self.db_pass}"
            f"@{self.db_host}:{self.db_port}/{self.db_name}"
        )

    model_config = SettingsConfigDict(env_file=[".env"], extra="ignore")


# 환경별 설정 클래스
class DevSettings(BaseSettingsConfig):
    model_config = SettingsConfigDict(env_file=[".env", ".env.dev"])


class PrdSettings(BaseSettingsConfig):
    model_config = SettingsConfigDict(env_file=[".env", ".env.prod"])


def get_settings() -> BaseSettingsConfig:
    """환경 변수에 따라 적절한 설정 객체를 반환하는 함수"""
    mode = os.getenv("MODE", "dev")
    if mode == "dev":
        return DevSettings()
    elif mode == "prd":
        return PrdSettings()
    else:
        raise ValueError(f"Invalid MODE environment variable: {mode}")


settings = get_settings()
