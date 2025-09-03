# Stage 1: Build with Maven + Java 21
FROM maven:3.9.11-amazoncorretto-21-al2023 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime with Java 21
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.war app.war
EXPOSE 8000
ENTRYPOINT ["java","-jar","app.war"]
