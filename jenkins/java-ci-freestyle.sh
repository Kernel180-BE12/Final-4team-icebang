#!/bin/bash
set -e

# Jenkins Freestyle Project Script for Java CI (User Service)
# This script replaces the GitHub Actions workflow: .github/workflows/ci-java.yml

echo "=============================="
echo "Java CI (Spring Boot) Build"
echo "=============================="

# Environment variables that should be set in Jenkins
# BUILD_NUMBER - Jenkins build number
# GIT_BRANCH - Git branch being built
# GIT_COMMIT - Git commit hash
# WORKSPACE - Jenkins workspace directory

# Change to the user service directory
cd "${WORKSPACE}/apps/user-service"

echo "Current working directory: $(pwd)"
echo "Git branch: ${GIT_BRANCH}"
echo "Git commit: ${GIT_COMMIT}"
echo "Build number: ${BUILD_NUMBER}"

# Check if this is a draft PR (skip if draft)
if [ "${IS_DRAFT_PR}" = "true" ]; then
    echo "Skipping build for draft PR"
    exit 0
fi

echo "Setting up JDK 21..."
# Ensure JDK 21 is available (should be pre-installed on Jenkins agent)
java -version || {
    echo "Java not found. Please install JDK 21 on the Jenkins agent."
    exit 1
}

# Grant execute permission for Gradle wrapper
echo "Setting up Gradle wrapper permissions..."
chmod +x ./gradlew

echo "Running Spotless Check (Lint)..."
./gradlew spotlessCheck

echo "Running Gradle Build (without tests)..."
./gradlew build -x test

echo "Running Tests..."
echo "- Running Unit Tests..."
./gradlew unitTest

echo "- Running Integration Tests..."
./gradlew integrationTest

# Run E2E tests only for main branch or tag builds
if [[ "${GIT_BRANCH}" == "origin/main" || "${GIT_BRANCH}" =~ ^origin/tags/.* ]]; then
    echo "- Running E2E Tests..."
    ./gradlew e2eTest
else
    echo "- Skipping E2E Tests (not main branch or tag build)"
fi

echo "=============================="
echo "Java CI completed successfully!"
echo "=============================="

# Build and push Docker image if this is a tag build
if [[ "${GIT_BRANCH}" =~ ^origin/tags/user-service-v.* ]]; then
    echo "Tag detected: ${GIT_BRANCH}"

    # Extract version from tag
    TAG_NAME=$(echo "${GIT_BRANCH}" | sed 's|origin/tags/||')
    IMAGE_TAG=$(echo "${TAG_NAME}" | sed 's|user-service-||')

    echo "Building Docker image with tag: ${IMAGE_TAG}"

    # Create build artifacts directory for archiving
    echo "Archiving build artifacts..."
    mkdir -p "${WORKSPACE}/build-artifacts"
    cp build/libs/*.jar "${WORKSPACE}/build-artifacts/" || echo "No JAR files found to archive"

    # Archive OpenAPI spec if available
    mkdir -p "${WORKSPACE}/openapi-spec"
    cp -r build/api-spec/* "${WORKSPACE}/openapi-spec/" 2>/dev/null || echo "No OpenAPI spec found to archive"

    # Login to GitHub Container Registry
    echo "${GITHUB_TOKEN}" | docker login ghcr.io -u "${GITHUB_USERNAME}" --password-stdin

    # Get repository name in lowercase
    REPO_LC=$(echo "${GIT_URL}" | sed 's|.*/||' | sed 's|\.git||' | tr '[:upper:]' '[:lower:]')

    echo "Building and pushing Docker image..."
    docker build -t "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/user-service:${IMAGE_TAG}" .
    docker tag "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/user-service:${IMAGE_TAG}" "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/user-service:latest"

    docker push "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/user-service:${IMAGE_TAG}"
    docker push "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/user-service:latest"

    echo "Docker image analysis..."
    docker history "ghcr.io/${GITHUB_USERNAME}/${REPO_LC}/user-service:${IMAGE_TAG}" --human --no-trunc

    echo "Docker image built and pushed successfully!"

    # Generate Swagger Documentation (if OpenAPI spec exists)
    if [ -f "${WORKSPACE}/openapi-spec/openapi3.yaml" ]; then
        echo "Generating Swagger UI documentation..."

        # Create a simple HTML file that includes Swagger UI
        mkdir -p "${WORKSPACE}/swagger-ui"
        cat > "${WORKSPACE}/swagger-ui/index.html" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>User Service API Documentation</title>
    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@3.25.0/swagger-ui.css" />
    <style>
        html {
            box-sizing: border-box;
            overflow: -moz-scrollbars-vertical;
            overflow-y: scroll;
        }
        *, *:before, *:after {
            box-sizing: inherit;
        }
        body {
            margin:0;
            background: #fafafa;
        }
    </style>
</head>
<body>
    <div id="swagger-ui"></div>
    <script src="https://unpkg.com/swagger-ui-dist@3.25.0/swagger-ui-bundle.js"></script>
    <script src="https://unpkg.com/swagger-ui-dist@3.25.0/swagger-ui-standalone-preset.js"></script>
    <script>
        window.onload = function() {
            const ui = SwaggerUIBundle({
                url: './openapi3.yaml',
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout"
            });
        };
    </script>
</body>
</html>
EOF

        # Copy OpenAPI spec to swagger-ui directory
        cp "${WORKSPACE}/openapi-spec/openapi3.yaml" "${WORKSPACE}/swagger-ui/"

        echo "Swagger UI generated in ${WORKSPACE}/swagger-ui/"
        echo "You can deploy this directory to a web server to view the documentation."
    else
        echo "No OpenAPI spec found, skipping Swagger documentation generation"
    fi
fi