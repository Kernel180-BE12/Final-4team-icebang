from ..core.config import settings
from sqlalchemy import create_engine, text
from app.core.config import settings

engine = create_engine(
    settings.db_url,
    pool_pre_ping=True,  # 연결 유효성 체크
)
