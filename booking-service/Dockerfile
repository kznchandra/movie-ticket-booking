FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /booking-service
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /booking-service
COPY --from=build /booking-service/target/*.jar booking-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "booking-service.jar"]