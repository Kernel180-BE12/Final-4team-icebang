# 1단계: 빌드 스테이지
# Java 21 JDK가 포함된 경량 이미지를 사용합니다.
# 이 단계에서 애플리케이션을 빌드합니다.
FROM openjdk:21-jdk-slim AS builder

# 컨테이너 내부에 작업 디렉토리를 생성하고 설정합니다.
WORKDIR /app

# Gradle Wrapper, 설정 파일, 소스 코드를 복사합니다.
# Docker의 레이어 캐싱을 활용하여 빌드 속도를 높입니다.
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# 애플리케이션 소스 코드를 복사합니다.
COPY src src

# 애플리케이션을 빌드하여 실행 가능한 JAR 파일을 만듭니다.
# `-x test`는 이미지 빌드 시 테스트를 건너뛰는 명령입니다.
RUN ./gradlew clean build -x test

---

# 2단계: 실행 스테이지
# 애플리케이션 실행에 필요한 Java 21 JRE만 포함된 경량 이미지를 사용합니다.
FROM openjdk:21-jre-slim

# 컨테이너 내부의 작업 디렉토리를 설정합니다.
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일을 복사합니다.
# `--from=builder` 옵션을 사용하여 첫 번째 단계에서 빌드된 JAR만 가져옵니다.
# 파일명은 `group`, `version`에 따라 `glt-korea-0.0.1-SNAPSHOT.jar`가 되므로,
# 이를 `app.jar`라는 간단한 이름으로 변경합니다.
COPY --from=builder /app/build/libs/glt-korea-0.0.1-SNAPSHOT.jar ./app.jar

# 애플리케이션이 외부 요청을 받을 포트를 노출합니다.
EXPOSE 8080

# 컨테이너 시작 시 실행될 명령어를 정의합니다.
CMD ["java", "-jar", "app.jar"]