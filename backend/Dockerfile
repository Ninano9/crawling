# Multi-stage build for Java Spring Boot application
FROM gradle:7.6-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 파일들 복사
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐싱 최적화)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN gradle bootJar --no-daemon

# 런타임 이미지
FROM openjdk:17-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 포트
EXPOSE 8080

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
