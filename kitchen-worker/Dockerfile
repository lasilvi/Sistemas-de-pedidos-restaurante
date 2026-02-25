FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Layer 1: Copy POM files only (cached unless pom.xml changes)
COPY pom.xml .
COPY kitchen-worker/pom.xml kitchen-worker/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY report-service/pom.xml report-service/pom.xml

# Layer 2: Download all dependencies (cached separately from source)
RUN mvn -pl kitchen-worker -am dependency:go-offline -DskipTests

# Layer 3: Copy source code (invalidated on code changes)
COPY kitchen-worker/src kitchen-worker/src

# Layer 4: Compile (uses cached dependencies, can download missing ones)
RUN mvn -pl kitchen-worker -am package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/kitchen-worker/target/kitchen-worker-*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
