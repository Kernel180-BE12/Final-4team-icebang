#!/bin/bash
set -e

# Jenkins Freestyle Project Script for Python CI (Pre-processing Service)
# This script replaces the GitHub Actions workflow: .github/workflows/ci-python.yml

echo "=============================="
echo "Python CI (FastAPI) Build"
echo "=============================="

# Environment variables that should be set in Jenkins
# BUILD_NUMBER - Jenkins build number
# GIT_BRANCH - Git branch being built
# GIT_COMMIT - Git commit hash
# WORKSPACE - Jenkins workspace directory

# Change to the pre-processing service directory
cd "${WORKSPACE}/apps/pre-processing-service"

echo "Current working directory: $(pwd)"
echo "Git branch: ${GIT_BRANCH}"
echo "Git commit: ${GIT_COMMIT}"
echo "Build number: ${BUILD_NUMBER}"

# Check if this is a draft PR (skip if draft)
# Note: This check would need to be configured in Jenkins pipeline parameters
if [ "${IS_DRAFT_PR}" = "true" ]; then
    echo "Skipping build for draft PR"
    exit 0
fi

echo "Setting up Python 3.11..."
# Ensure Python 3.11 is available (should be pre-installed on Jenkins agent)
python3.11 --version || {
    echo "Python 3.11 not found. Please install Python 3.11 on the Jenkins agent."
    exit 1
}

# Create a virtual environment
echo "Creating virtual environment..."
python3.11 -m venv .venv
source .venv/bin/activate

# Install Poetry if not available
echo "Installing Poetry..."
if ! command -v poetry &> /dev/null; then
    curl -sSL https://install.python-poetry.org | python3 -
    export PATH="$HOME/.local/bin:$PATH"
fi

# Configure Poetry
poetry config virtualenvs.create true
poetry config virtualenvs.in-project true

echo "Installing dependencies..."
poetry install --no-interaction --no-root

echo "Running formatter check (Black)..."
poetry run black --check .

# Uncomment when tests are ready
# echo "Running tests with Pytest..."
# export DB_HOST=localhost
# export DB_PORT=3306
# export DB_USER=test_user
# export DB_PASS=test_pass
# export DB_NAME=test_db
# export ENV_NAME=test
# poetry run pytest

echo "=============================="
echo "Python CI completed successfully!"
echo "=============================="

# Build and push Docker image if this is a tag build
if [[ "${GIT_BRANCH}" =~ ^origin/tags/pre-processing-v.* ]]; then
    echo "Tag detected: ${GIT_BRANCH}"

    # Extract version from tag
    TAG_NAME=$(echo "${GIT_BRANCH}" | sed 's|origin/tags/||')
    IMAGE_TAG=$(echo "${TAG_NAME}" | sed 's|pre-processing-||')

    echo "Building Docker image with tag: ${IMAGE_TAG}"

    # Login to GitHub Container Registry
    echo "${GITHUB_TOKEN}" | docker login ghcr.io -u "${GITHUB_USERNAME}" --password-stdin

    # Get repository name in lowercase
    REPO_LC=$(echo "${GIT_URL}" | sed 's|.*/||' | sed 's|\.git||' | tr '[:upper:]' '[:lower:]')

    echo "Building and pushing Docker image..."
    docker build -t "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/pre-processing-service:${IMAGE_TAG}" .
    docker tag "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/pre-processing-service:${IMAGE_TAG}" "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/pre-processing-service:latest"

    docker push "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/pre-processing-service:${IMAGE_TAG}"
    docker push "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/pre-processing-service:latest"

    echo "Docker image analysis..."
    docker history "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/pre-processing-service:${IMAGE_TAG}" --human --no-trunc

    echo "Docker image built and pushed successfully!"
fi