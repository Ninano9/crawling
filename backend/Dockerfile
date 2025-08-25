# Multi-stage build for Java Spring Boot application
FROM gradle:7.6-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper와 설정 파일들 복사
COPY gradlew .
COPY gradle/ ./gradle/
COPY build.gradle .
COPY settings.gradle .

# Gradle wrapper 실행 권한 부여
RUN chmod +x gradlew

# 의존성 다운로드 (캐싱 최적화)
RUN ./gradlew dependencies --no-daemon || echo "Some dependencies may be missing, continuing..."

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드 (더 관대한 빌드)
RUN ./gradlew bootJar --no-daemon --stacktrace || \
    ./gradlew build --no-daemon --stacktrace

# 런타임 이미지
FROM eclipse-temurin:17-jre-alpine

# 필요한 패키지 설치 (Python, FFmpeg, curl)
RUN apk add --no-cache python3 py3-pip ffmpeg curl

# Edge-TTS 설치
RUN pip3 install edge-tts

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 포트
EXPOSE 8080

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV TTS_PROVIDER=edge-tts
ENV VIDEO_GENERATION_ENABLED=true

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
