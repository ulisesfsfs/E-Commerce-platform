FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY discovery-server/src discovery-server/src
RUN --mount=type=cache,target=/root/.m2 \
    cd discovery-server && ../mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/discovery-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
