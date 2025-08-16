# 멀티스테이지 빌드
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
# Gradle 파일들을 먼저 복사하여 의존성 캐싱 최적화
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew .
# 의존성 다운로드
RUN ./gradlew dependencies
# 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew bootJar
# 실행 단계
FROM openjdk:21-jre-slim
WORKDIR /app
# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar
# 애플리케이션 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]