# pydantic_settings에서 SettingsConfigDict를 추가로 import 합니다.
from pydantic_settings import BaseSettings, SettingsConfigDict
import os
from typing import Optional


# 공통 설정을 위한 BaseSettings
class BaseSettingsConfig(BaseSettings):

    # db_url 대신 개별 필드를 정의합니다.
    db_host: str
    db_port: str
    db_user: str
    db_pass: str
    db_name: str
    env_name: str = ".dev"

    @property
    def db_url(self) -> str:
        """개별 필드를 사용하여 DB URL을 동적으로 생성"""
        return f"postgresql://{self.db_user}:{self.db_pass}@{self.db_host}:{self.db_port}/{self.db_name}"

    model_config = SettingsConfigDict(env_file=['.env'])


# 환경별 설정 클래스
class DevSettings(BaseSettingsConfig):
    model_config = SettingsConfigDict(env_file=['.env', '.dev.env'])


class PrdSettings(BaseSettingsConfig):
    model_config = SettingsConfigDict(env_file=['.env', '.prd.env'])

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