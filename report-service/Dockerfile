# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /build

# Copy dependency descriptors first (cache optimization)
COPY pom.xml .
COPY report-service/pom.xml report-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY kitchen-worker/pom.xml kitchen-worker/pom.xml

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl report-service -am || true

# Copy source code
COPY report-service/src report-service/src

# Build application (include tests for production builds)
RUN mvn -pl report-service -am clean package -DskipTests=false && \
    mv /build/report-service/target/report-service-*.jar /build/app.jar

# Runtime stage
FROM eclipse-temurin:17.0.10_7-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy artifact from build stage
COPY --from=build --chown=spring:spring /build/app.jar app.jar

# Switch to non-root user
USER spring:spring

# Spring Boot optimizations
EXPOSE 8082
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
