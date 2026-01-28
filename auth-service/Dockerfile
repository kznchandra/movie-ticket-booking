FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /auth-service
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /auth-service
COPY --from=build /auth-service/target/*.jar auth-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "auth-service.jar"]