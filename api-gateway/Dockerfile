# =========================
# Build stage
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom + src nguyên gốc
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src

# Build JAR executable, skip test
RUN mvn clean package -f api-gateway/pom.xml -Dmaven.test.skip=true

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy JAR đúng từ target module gốc
COPY --from=build /app/api-gateway/target/api-gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
