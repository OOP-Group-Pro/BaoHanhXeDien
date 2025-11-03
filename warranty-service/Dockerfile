FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY base-pom/pom.xml .
RUN mvn install -N -f pom.xml

COPY warranty-service/pom.xml warranty-service/
RUN mvn dependency:go-offline -f warranty-service/pom.xml

COPY warranty-service/src warranty-service/src
RUN mvn clean package -f warranty-service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/warranty-service/target/warranty-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
