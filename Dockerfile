FROM maven:3.9.1-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /root/.m2/* /root/.m2
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]