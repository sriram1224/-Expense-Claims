# Stage 1: Build JAR inside Docker (no target/ commit needed)
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production JAR skipping test execution
RUN mvn clean package -DskipTests

# Stage 2: Minimal JRE Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy packaged JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

ENV PORT=10000
EXPOSE 10000

# Bind to 0.0.0.0 on Render's dynamic PORT
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Dserver.address=0.0.0.0 -jar app.jar"]
