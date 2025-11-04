# --- GIAI ĐOẠN 1: Build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy toàn bộ project (bao gồm parent pom.xml và các module con)
COPY ../ /app

# Chuyển vào module cần build
WORKDIR /app/part-service

# Build module part-service và các module phụ thuộc
RUN mvn -f /app/pom.xml clean package -pl part-service -am -DskipTests


# --- GIAI ĐOẠN 2: Run ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy file jar từ stage build
COPY --from=build /app/part-service/target/part-service-*.jar app.jar

EXPOSE 8004
ENTRYPOINT ["java", "-jar", "app.jar"]
