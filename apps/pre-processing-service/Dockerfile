# ---- builder ----
FROM python:3.11-slim AS builder
WORKDIR /app

# OS 패키지 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    build-essential \
  && rm -rf /var/lib/apt/lists/*

# Poetry 설치
RUN curl -sSL https://install.python-poetry.org | python3 -
ENV PATH="/root/.local/bin:$PATH"
RUN poetry self add "poetry-plugin-export>=1.7.0"

# 런타임 가상환경
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# 의존성 해결 → requirements로 export → pip로 설치(= 반드시 /opt/venv에 설치됨)
COPY pyproject.toml poetry.lock ./
RUN poetry export --without dev -f requirements.txt -o requirements.txt \
 && pip install --no-cache-dir -r requirements.txt
# ---- runtime ----
FROM python:3.11-slim AS final
WORKDIR /app

# OS 패키지 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    unzip \
    curl \
    gnupg \
    ca-certificates \
    libgl1-mesa-dri \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender1 \
    libgomp1 \
    libgthread-2.0-0 \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# 빌드된 가상환경 복사
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# 앱 소스 복사
COPY . .

# 실행 명령
ENTRYPOINT ["gunicorn", "-k", "uvicorn.workers.UvicornWorker", "app.main:app", \
           "-b", "0.0.0.0:8000", \
           "--timeout", "240", \
           "--workers", "1", \
           "--max-requests", "50", \
           "--max-requests-jitter", "10", \
           "--preload"]