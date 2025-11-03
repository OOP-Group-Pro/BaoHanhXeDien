FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY base-pom/pom.xml .
RUN mvn install -N -f pom.xml

COPY campaign-service/pom.xml campaign-service/
RUN mvn dependency:go-offline -f campaign-service/pom.xml

COPY campaign-service/src campaign-service/src
RUN mvn clean package -f campaign-service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/campaign-service/target/campaign-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
