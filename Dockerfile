# Build stage (CI/로컬/서버 재현성↑)
FROM eclipse-temurin:21-jdk AS builder

# 작업 디렉토리 생성
WORKDIR /app

# 보안을 위해 비루트(non-root) 사용자 생성 및 전환
# 기존의 짧은 옵션(-S, -g, -G) 대신 풀네임 옵션을 사용합니다.
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup --disabled-password appuser

USER appuser
USER appuser

# 외부(GitHub Actions 등)에서 빌드된 jar 파일을 이미지 내부로 복사
# 빌드 결과물 경로가 build/libs/*.jar 인지 확인이 필요합니다.
COPY build/libs/*.jar app.jar

EXPOSE 8080

# JVM 메모리 설정
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0"

# 앱 실행 - JVM 옵션을 포함하여 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]