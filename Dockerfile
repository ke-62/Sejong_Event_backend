# syntax=docker/dockerfile:1

# --- build stage : Gradle 래퍼로 bootJar 생성 ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 레이어 캐시
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# --- runtime stage : JRE + 비루트 유저 ---
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --chown=spring:spring --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
