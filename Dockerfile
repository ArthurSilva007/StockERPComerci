FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN useradd --system --create-home --uid 10001 nexerp
COPY --from=build /workspace/target/nexerp-backend-0.0.1-SNAPSHOT.jar /app/app.jar
RUN chown nexerp:nexerp /app/app.jar
USER nexerp

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
