# Jenkins Freestyle Projects Setup

이 문서는 GitHub Actions 워크플로우를 Jenkins 프리스타일 프로젝트로 변환하는 방법을 설명합니다.

## 개요

기존 GitHub Actions 워크플로우들을 Jenkins 프리스타일 프로젝트로 변환했습니다:

1. **python-ci-freestyle.sh** - Python CI (Pre-processing Service)
2. **java-ci-freestyle.sh** - Java CI (User Service)
3. **python-deploy-freestyle.sh** - Python 배포 (FastAPI)
4. **java-deploy-freestyle.sh** - Java 배포 (Spring Boot)

## Jenkins 프로젝트 설정

### 1. Python CI 프로젝트 (Pre-processing Service)

**프로젝트명**: `pre-processing-ci`

**소스 코드 관리**:
- Git Repository URL: `https://github.com/Kernel180-BE12/Final-4team-icebang.git`
- Branch Specifier: `*/develop`, `*/main`, `*/release/**`, `pre-processing-v*`

**빌드 유발**:
- Poll SCM: `H/5 * * * *` (5분마다 체크)
- GitHub hook trigger for GITScm polling 체크

**빌드 환경**:
- Delete workspace before build starts 체크

**Build Steps**:
- Execute shell: `./jenkins/python-ci-freestyle.sh`

**필요한 환경변수**:
```
GITHUB_TOKEN=<GitHub Token>
GITHUB_USERNAME=<GitHub Username>
IS_DRAFT_PR=false
```

### 2. Java CI 프로젝트 (User Service)

**프로젝트명**: `user-service-ci`

**소스 코드 관리**:
- Git Repository URL: `https://github.com/Kernel180-BE12/Final-4team-icebang.git`
- Branch Specifier: `*/develop`, `*/main`, `*/release/**`, `user-service-v*`

**빌드 유발**:
- Poll SCM: `H/5 * * * *`
- GitHub hook trigger for GITScm polling 체크

**빌드 환경**:
- Delete workspace before build starts 체크

**Build Steps**:
- Execute shell: `./jenkins/java-ci-freestyle.sh`

**필요한 환경변수**:
```
GITHUB_TOKEN=<GitHub Token>
GITHUB_USERNAME=<GitHub Username>
IS_DRAFT_PR=false
```

### 3. Python 배포 프로젝트 (FastAPI)

**프로젝트명**: `pre-processing-deploy`

**소스 코드 관리**:
- Git Repository URL: `https://github.com/Kernel180-BE12/Final-4team-icebang.git`
- Branch Specifier: `pre-processing-v*`

**빌드 유발**:
- Build after other projects are built: `pre-processing-ci` (stable builds only)

**Build Steps**:
- Execute shell: `./jenkins/python-deploy-freestyle.sh`

**필요한 환경변수**:
```
FASTAPI_SERVER_HOST=<FastAPI Server IP>
SERVER_SSH_KEY=<SSH Private Key>
GITHUB_TOKEN=<GitHub Token>
GITHUB_USERNAME=<GitHub Username>
DISCORD_WEBHOOK_URL=<Discord Webhook URL>

# Database
DB_HOST=<Database Host>
DB_PORT=<Database Port>
DB_USER=<Database User>
DB_PASS=<Database Password>
DB_NAME=<Database Name>

# Logging
LOKI_HOST=<Loki Host>
LOKI_USERNAME=<Loki Username>
LOKI_PASSWORD=<Loki Password>

# Monitoring
GRAFANA_CLOUD_PROMETHEUS_URL=<Prometheus URL>
GRAFANA_CLOUD_PROMETHEUS_USER=<Prometheus User>
GRAFANA_CLOUD_API_KEY=<Grafana API Key>

# Application
ENV_NAME=production
OPENAI_API_KEY=<OpenAI API Key>
```

### 4. Java 배포 프로젝트 (Spring Boot)

**프로젝트명**: `user-service-deploy`

**소스 코드 관리**:
- Git Repository URL: `https://github.com/Kernel180-BE12/Final-4team-icebang.git`
- Branch Specifier: `user-service-v*`

**빌드 유발**:
- Build after other projects are built: `user-service-ci` (stable builds only)

**Build Steps**:
- Execute shell: `./jenkins/java-deploy-freestyle.sh`

**필요한 환경변수**:
```
SERVER_HOST=<Server IP>
SERVER_SSH_KEY=<SSH Private Key>
GITHUB_TOKEN=<GitHub Token>
GITHUB_USERNAME=<GitHub Username>
DISCORD_WEBHOOK_URL=<Discord Webhook URL>

# Database
DB_HOST=<Database Host>
DB_PORT=<Database Port>
DB_USER=<Database User>
DB_PASS=<Database Password>
DB_NAME=<Database Name>

# Logging
LOKI_HOST=<Loki Host>
LOKI_USERNAME=<Loki Username>
LOKI_PASSWORD=<Loki Password>

# Monitoring
GRAFANA_CLOUD_PROMETHEUS_URL=<Prometheus URL>
GRAFANA_CLOUD_PROMETHEUS_USER=<Prometheus User>
GRAFANA_CLOUD_API_KEY=<Grafana API Key>

# Application
ENV_NAME=production
FASTAPI_SERVER_HOST=<FastAPI Server Host>
```

## Jenkins 에이전트 요구사항

### 시스템 요구사항
- Ubuntu 20.04 LTS 이상
- Docker 설치 및 구성
- Git 설치
- 네트워크 접근: GitHub, AWS EC2, Docker Registry

### Python CI 에이전트
```bash
# Python 3.11 설치
sudo apt update
sudo apt install -y python3.11 python3.11-venv python3.11-dev

# Poetry 설치
curl -sSL https://install.python-poetry.org | python3 -
```

### Java CI 에이전트
```bash
# OpenJDK 21 설치
sudo apt update
sudo apt install -y openjdk-21-jdk

# JAVA_HOME 설정
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

### 공통 요구사항
```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker jenkins

# Git 설치
sudo apt install -y git

# SSH 클라이언트 설치
sudo apt install -y openssh-client
```

## 보안 고려사항

1. **SSH 키 관리**: Jenkins Credentials에 SSH 개인키를 안전하게 저장
2. **환경변수**: 민감한 정보는 Jenkins Credentials를 통해 관리
3. **Docker 레지스트리**: GITHUB_TOKEN은 적절한 권한으로 제한
4. **네트워크**: 필요한 포트만 열어두고 방화벽 설정

## 트러블슈팅

### 일반적인 문제

1. **Permission denied (publickey)**: SSH 키가 올바르게 설정되지 않음
   - Jenkins에서 SSH 키 credential 확인
   - 대상 서버의 authorized_keys 확인

2. **Docker login failed**: GitHub Token 권한 부족
   - Token에 packages:write 권한 확인
   - Token 만료 여부 확인

3. **Build timeout**: 빌드 시간이 너무 오래 걸림
   - Jenkins 프로젝트 설정에서 타임아웃 시간 조정
   - 캐시 설정 확인

### 로그 확인

- Jenkins 콘솔 출력에서 상세 로그 확인
- 대상 서버에서 Docker 로그 확인: `docker-compose logs -f`
- Discord 알림으로 배포 상태 모니터링

## 참고사항

- 스크립트들은 실행 권한이 필요합니다: `chmod +x jenkins/*.sh`
- 태그 기반 배포를 위해서는 Git 태깅 전략을 유지해야 합니다
- 배포 스크립트는 CI 빌드가 성공한 후에만 실행됩니다