# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /build

# Copy dependency descriptors first (cache optimization)
COPY pom.xml .
COPY kitchen-worker/pom.xml kitchen-worker/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY report-service/pom.xml report-service/pom.xml

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl kitchen-worker -am || true

# Copy source code
COPY kitchen-worker/src kitchen-worker/src

# Build application (include tests for production builds)
RUN mvn -pl kitchen-worker -am clean package -DskipTests=false && \
    mv /build/kitchen-worker/target/kitchen-worker-*.jar /build/app.jar

# Runtime stage
FROM eclipse-temurin:17.0.10_7-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy artifact from build stage
COPY --from=build --chown=spring:spring /build/app.jar app.jar

# Switch to non-root user
USER spring:spring

# Kitchen worker is AMQP consumer - no HTTP port exposed
# Spring Boot optimizations
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
