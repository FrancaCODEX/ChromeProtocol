FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B verify

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system chrome && useradd --system --gid chrome chrome && mkdir data && chown chrome:chrome data
COPY --from=build /build/target/chromeprotocol-0.2.0.jar app.jar
USER chrome
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
