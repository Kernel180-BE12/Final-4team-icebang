#!/bin/bash
set -e

# Jenkins Freestyle Project Script for Java Deployment (Spring Boot)
# This script replaces the GitHub Actions workflow: .github/workflows/deploy-java.yml

echo "=============================="
echo "Spring Boot Deployment to AWS EC2"
echo "=============================="

# Environment variables that should be set in Jenkins
# BUILD_NUMBER - Jenkins build number
# GIT_BRANCH - Git branch being built
# GIT_COMMIT - Git commit hash
# WORKSPACE - Jenkins workspace directory
# SERVER_HOST - Target server host
# SERVER_SSH_KEY - SSH private key for server access
# GITHUB_TOKEN - GitHub token for Docker registry
# GITHUB_USERNAME - GitHub username
# DISCORD_WEBHOOK_URL - Discord webhook for notifications

# Check if this is a successful CI build for a user-service tag
if [[ ! "${GIT_BRANCH}" =~ ^origin/tags/user-service-v.* ]]; then
    echo "Not a user-service tag build, skipping deployment"
    exit 0
fi

echo "Current working directory: ${WORKSPACE}"
echo "Git branch: ${GIT_BRANCH}"
echo "Git commit: ${GIT_COMMIT}"
echo "Build number: ${BUILD_NUMBER}"

# Extract version from tag
TAG_NAME=$(echo "${GIT_BRANCH}" | sed 's|origin/tags/||')
echo "Deploying tag: ${TAG_NAME}"

# Create environment file for production
echo "Creating production environment file..."
cat > .env.prod << EOF
DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
DB_USER=${DB_USER}
DB_PASS=${DB_PASS}
DB_NAME=${DB_NAME}
LOKI_HOST=${LOKI_HOST}
LOKI_USERNAME=${LOKI_USERNAME}
LOKI_PASSWORD=${LOKI_PASSWORD}
ENV_NAME=${ENV_NAME}
FASTAPI_SERVER_HOST=${FASTAPI_SERVER_HOST}
GRAFANA_CLOUD_PROMETHEUS_URL=${GRAFANA_CLOUD_PROMETHEUS_URL}
GRAFANA_CLOUD_PROMETHEUS_USER=${GRAFANA_CLOUD_PROMETHEUS_USER}
GRAFANA_CLOUD_API_KEY=${GRAFANA_CLOUD_API_KEY}
EOF

# Get repository name in lowercase
REPO_LC=$(echo "${GIT_URL}" | sed 's|.*/||' | sed 's|\.git||' | tr '[:upper:]' '[:lower:]')
export REPO_LC

echo "Deploying to server: ${SERVER_HOST}"

# Setup SSH key
echo "Setting up SSH key..."
mkdir -p ~/.ssh
echo "${SERVER_SSH_KEY}" > ~/.ssh/deploy_key
chmod 600 ~/.ssh/deploy_key

# Function to send Discord notification
send_discord_notification() {
    local status="$1"
    local message="$2"

    if [ -n "${DISCORD_WEBHOOK_URL}" ]; then
        curl -H "Content-Type: application/json" \
             -X POST \
             -d "{\"content\": \"${message}\"}" \
             "${DISCORD_WEBHOOK_URL}" || echo "Failed to send Discord notification"
    fi
}

# Trap function for cleanup and failure notification
cleanup_and_notify() {
    local exit_code=$?
    if [ $exit_code -ne 0 ]; then
        send_discord_notification "failure" "**배포 실패**
**Repository:** ${REPO_LC}
**Tag:** ${TAG_NAME}
**Error:** 배포 중 오류가 발생했습니다.
**Build:** ${BUILD_URL}"
    fi
    rm -f ~/.ssh/deploy_key
}

trap cleanup_and_notify EXIT

# Copy docker compose files to EC2
echo "Copying docker compose files to server..."
scp -i ~/.ssh/deploy_key -o StrictHostKeyChecking=no \
    docker/production/docker-compose.yml \
    ubuntu@${SERVER_HOST}:~/app/docker/production/

# Copy .env.prod file to EC2
echo "Copying environment file to server..."
scp -i ~/.ssh/deploy_key -o StrictHostKeyChecking=no \
    .env.prod \
    ubuntu@${SERVER_HOST}:~/app/docker/production/

# Copy configuration files
echo "Copying configuration files to server..."
scp -i ~/.ssh/deploy_key -o StrictHostKeyChecking=no \
    docker/production/Caddyfile \
    ubuntu@${SERVER_HOST}:~/app/docker/production/

scp -i ~/.ssh/deploy_key -o StrictHostKeyChecking=no \
    docker/production/promtail-config.yml \
    ubuntu@${SERVER_HOST}:~/app/docker/production/

scp -i ~/.ssh/deploy_key -o StrictHostKeyChecking=no \
    docker/production/agent-config.yml \
    ubuntu@${SERVER_HOST}:~/app/docker/production/

# Deploy on EC2
echo "Deploying application on EC2..."
ssh -i ~/.ssh/deploy_key -o StrictHostKeyChecking=no ubuntu@${SERVER_HOST} << EOF
set -e

cd ~/app/docker/production

echo "Logging into GitHub Container Registry..."
echo "${GITHUB_TOKEN}" | docker login ghcr.io -u ${GITHUB_USERNAME} --password-stdin

echo "Pulling latest Docker images..."
docker-compose pull

echo "Stopping existing containers..."
docker-compose down

echo "Starting new containers..."
docker-compose up -d

echo "Waiting for containers to start..."
sleep 5
docker-compose ps

echo "Waiting for containers to become healthy..."
for i in {1..30}; do
    unhealthy=\$(docker ps --filter "health=unhealthy" --format "{{.Names}}")
    starting=\$(docker ps --filter "health=starting" --format "{{.Names}}")
    if [ -z "\$unhealthy" ] && [ -z "\$starting" ]; then
        echo "All containers are healthy!"
        break
    fi
    echo "Waiting... (\$i/30)"
    sleep 2
done

echo "Cleaning up unused Docker images..."
docker image prune -f

echo "Deployment completed successfully!"
EOF

echo "=============================="
echo "Spring Boot deployment completed successfully!"
echo "=============================="

# Send success notification
send_discord_notification "success" "**배포 성공**
**Repository:** ${REPO_LC}
**Tag:** ${TAG_NAME}
**Server:** ${SERVER_HOST}
**Status:** Success!"

# Cleanup
rm -f ~/.ssh/deploy_key