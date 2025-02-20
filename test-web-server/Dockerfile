FROM maven:3-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/target/test-web-server-*-with-dependencies.jar /app/test-web-server.jar
ENTRYPOINT ["java", "-jar", "/app/test-web-server.jar"]