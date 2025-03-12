FROM maven:3.9.1-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src

ENV TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal
ENV DOCKER_HOST=unix:///var/run/docker.sock

RUN mvn test
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]