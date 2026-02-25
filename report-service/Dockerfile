FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Layer 1: Copy POM files only (cached unless pom.xml changes)
COPY pom.xml .
COPY report-service/pom.xml report-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY kitchen-worker/pom.xml kitchen-worker/pom.xml

# Layer 2: Download all dependencies (cached separately from source)
RUN mvn -pl report-service -am dependency:go-offline -DskipTests

# Layer 3: Copy source code (invalidated on code changes)
COPY report-service/src report-service/src

# Layer 4: Compile (uses cached dependencies, can download missing ones)
RUN mvn -pl report-service -am package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/report-service/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
