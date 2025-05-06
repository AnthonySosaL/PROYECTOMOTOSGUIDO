# Etapa 1: construir con Maven
FROM maven:3.8.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: sólo runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/backmoto-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -jar app.jar --server.port=${PORT:-8080}"]
