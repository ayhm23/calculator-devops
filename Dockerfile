FROM maven:3.9.16-eclipse-temurin-17 AS build
WORKDIR /app

# dependencies get their own layer, only rebuilt when pom.xml changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17.0.20.1_1-jre-alpine-3.24
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /app/target/calculator.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
