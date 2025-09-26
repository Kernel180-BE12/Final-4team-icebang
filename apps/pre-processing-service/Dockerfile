# ---- builder ----
FROM python:3.11-slim AS builder
WORKDIR /app

# 필수 OS 패키지 (기존 + Chrome 설치용 패키지 추가)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    wget \
    unzip \
    gnupg \
    ca-certificates \
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

# Chrome과 ChromeDriver 설치를 위한 패키지 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    unzip \
    curl \
    gnupg \
    ca-certificates \
    xclip \
    xsel \
    xvfb \
    && rm -rf /var/lib/apt/lists/*

# Chrome 설치 (블로그 방식 - 직접 .deb 파일 다운로드)
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt-get update \
    && apt-get install -y ./google-chrome-stable_current_amd64.deb \
    && rm ./google-chrome-stable_current_amd64.deb \
    && rm -rf /var/lib/apt/lists/*

# MeCab & 사전 설치 (형태소 분석 의존)
RUN apt-get update && apt-get install -y --no-install-recommends \
    mecab \
    libmecab-dev \
    wget \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# 한국어 사전 수동 설치
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

ENV DISPLAY=:99
RUN echo '#!/bin/bash\nXvfb :99 -screen 0 1920x1080x24 &\nsleep 2\nexec "$@"' > /start.sh && chmod +x /start.sh

# (권장 대안) 코드에서 uvicorn import 안 하고 프로세스 매니저로 실행하려면:
ENTRYPOINT ["/start.sh", "gunicorn", "-k", "uvicorn.workers.UvicornWorker", "app.main:app", "-b", "0.0.0.0:8000", "--timeout", "120"]