# 멀티 스테이지 빌드를 사용하여 이미지 크기 최적화
FROM gradle:8-jdk17 AS build
WORKDIR /app

# Gradle 캐시를 활용하기 위해 의존성 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 활용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle build --no-daemon -x test

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출 (Cloud Run은 PORT 환경 변수를 사용)
EXPOSE 8090

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]