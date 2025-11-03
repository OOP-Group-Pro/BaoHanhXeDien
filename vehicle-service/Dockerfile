FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY base-pom/pom.xml .
RUN mvn install -N -f pom.xml

COPY vehicle-service/pom.xml vehicle-service/
RUN mvn dependency:go-offline -f vehicle-service/pom.xml

COPY vehicle-service/src vehicle-service/src
RUN mvn clean package -f vehicle-service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/vehicle-service/target/vehicle-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
