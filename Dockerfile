# Multi-stage Dockerfile for root context deployment
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy backend pom.xml and source code
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src

# Build production JAR skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime Environment
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy compiled JAR
COPY --from=builder /build/target/*.jar app.jar

ENV PORT=10000
EXPOSE 10000

# Bind to 0.0.0.0 on Render dynamic PORT
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Dserver.address=0.0.0.0 -jar app.jar"]
