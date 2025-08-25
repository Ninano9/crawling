# 빌드 스테이지
FROM gradle:7.6-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /app

# 모든 파일 복사 (간단하게)
COPY . .

# Gradle wrapper 실행 권한 부여
RUN chmod +x gradlew || echo "gradlew not found, using gradle directly"

# 빌드 실행 (여러 방법 시도)
RUN ./gradlew clean bootJar --no-daemon || \
    gradle clean bootJar --no-daemon || \
    gradle clean build --no-daemon

# 런타임 이미지
FROM eclipse-temurin:17-jre-alpine

# 필요한 패키지 설치 (Python, FFmpeg, curl)
RUN apk add --no-cache python3 py3-pip ffmpeg curl python3-dev

# Edge-TTS 설치 (가상환경 없이)
RUN pip3 install edge-tts --break-system-packages

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
