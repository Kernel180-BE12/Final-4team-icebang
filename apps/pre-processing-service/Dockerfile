# ---- builder ----
FROM python:3.11-slim AS builder
WORKDIR /app

# 시스템 의존성 (필요 최소한)
RUN apt-get update && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

# CPU-only PyTorch 인덱스
ENV PIP_INDEX_URL=https://pypi.org/simple
ENV PIP_EXTRA_INDEX_URL=https://download.pytorch.org/whl/cpu

# Poetry 설치
RUN curl -sSL https://install.python-poetry.org | python3 - && \
    /root/.local/bin/poetry --version

ENV PATH="/root/.local/bin:$PATH"

# venv 생성 (site-packages 통째 복사 대신 venv만 옮길 것)
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY pyproject.toml poetry.lock ./
RUN poetry config virtualenvs.create false \
 && poetry install --no-root --without dev\
 && pip install --no-cache-dir gunicorn uvicorn \
 && rm -rf /root/.cache/pip

# ---- final ----
FROM python:3.11-slim AS final
WORKDIR /app

COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY ./app ./app
EXPOSE 8000

CMD ["gunicorn", "-k", "uvicorn.workers.UvicornWorker", "app.main:app", "--bind", "0.0.0.0:8000"]

