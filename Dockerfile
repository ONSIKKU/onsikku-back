# 실행 환경 설정
FROM eclipse-temurin:21-jdk-alpine

# 작업 디렉토리 생성
WORKDIR /app

# 빌드된 JAR 파일을 이미지 내부로 복사 - GitHub Actions가 빌드한 jar 파일의 경로를 지정
COPY build/libs/onsikku-back-0.0.1-SNAPSHOT.jar app.jar

# 앱 실행
ENTRYPOINT ["java", "-jar", "app.jar"]