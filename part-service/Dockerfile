FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY base-pom/pom.xml .
RUN mvn install -N -f pom.xml

COPY part-service/pom.xml part-service/
RUN mvn dependency:go-offline -f part-service/pom.xml

COPY part-service/src part-service/src
RUN mvn clean package -f part-service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/part-service/target/part-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
