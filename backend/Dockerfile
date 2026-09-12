# Multi-stage Docker build for OptiAlloc Spring Boot Backend
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven Wrapper & POM
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B || true

# Copy source files and build application package
COPY src src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose internal port 8080
EXPOSE 8080

# Environment variable default for Render PORT
ENV PORT=8080

# Run Spring Boot Application
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
