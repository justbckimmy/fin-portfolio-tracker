# 1단계(build stage): 소스 코드를 빌드해서 실행 가능한 jar 파일을 만든다
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# 2단계(run stage): 빌드 결과물(jar)만 가지고 훨씬 가벼운 이미지로 실행한다
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
