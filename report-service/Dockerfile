FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY report-service/pom.xml report-service/
RUN mvn dependency:go-offline -B -pl report-service

COPY report-service/src report-service/src
RUN mvn clean package -DskipTests -pl report-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/report-service/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
