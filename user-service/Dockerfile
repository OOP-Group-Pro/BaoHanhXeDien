FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY base-pom/pom.xml .
RUN mvn install -N -f pom.xml

COPY user-service/pom.xml user-service/
RUN mvn dependency:go-offline -f user-service/pom.xml

COPY user-service/src user-service/src
RUN mvn clean package -f user-service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/user-service/target/user-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
