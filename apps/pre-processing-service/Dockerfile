# ---- builder ----
FROM python:3.11-slim AS builder
WORKDIR /app

# 필수 OS 패키지 설치
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

# poetry → requirements로 export → pip로 설치
COPY pyproject.toml poetry.lock ./
RUN poetry export --without dev -f requirements.txt -o requirements.txt \
 && pip install --no-cache-dir -r requirements.txt
# ---- runtime ----
FROM python:3.11-slim AS final
WORKDIR /app

# Chrome과 ChromeDriver 설치를 위한 패키지 설치 (삭제 예정 - 마운트 방식)
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    unzip \
    curl \
    gnupg \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Chrome 설치 (삭제 예정 - 마운트 방식)
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt-get update \
    && apt-get install -y ./google-chrome-stable_current_amd64.deb \
    && rm ./google-chrome-stable_current_amd64.deb \
    && rm -rf /var/lib/apt/lists/*

# MeCab & 사전 설치 (삭제 예정 - 마운트 방식)
RUN apt-get update && apt-get install -y --no-install-recommends \
    mecab \
    libmecab-dev \
    wget \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# 한국어 사전 수동 설치 (삭제 예정 - 마운트 방식)
RUN cd /tmp && \
    wget https://bitbucket.org/eunjeon/mecab-ko-dic/downloads/mecab-ko-dic-2.1.1-20180720.tar.gz && \
    tar -zxf mecab-ko-dic-2.1.1-20180720.tar.gz && \
    cd mecab-ko-dic-2.1.1-20180720 && \
    ./configure && \
    make && \
    make install && \
    cd / && rm -rf /tmp/mecab-ko-dic-*

# /opt/venv 복사
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# 앱 소스
COPY . .

# gunicorn으로 FastAPI 앱 실행 - 타임아웃 240초 설정
ENTRYPOINT ["/opt/venv/bin/gunicorn", "-k", "uvicorn.workers.UvicornWorker", "app.main:app", "-b", "0.0.0.0:8000", "--timeout", "240"]