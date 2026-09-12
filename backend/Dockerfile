# Multi-stage Docker build for OptiAlloc Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and source files
COPY pom.xml .
COPY src src

# Package application JAR skipping unit tests (tests are verified in CI/dev)
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose internal ports (8080 default, 10000 Render default)
EXPOSE 8080 10000

# Environment variable default for Render PORT
ENV PORT=10000

# Run Spring Boot Application binding to ${PORT}
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]
