# --- GIAI ĐOẠN 1: Build (Dùng Maven + JDK 21) ---
ARG MODULE_NAME=campaign-service
FROM maven:3.9-eclipse-temurin-21 AS build

# Tên của module này (Dùng cho bước 4, 5, 6)
ARG MODULE_NAME
WORKDIR /app

# 1. Copy POM cha
COPY pom.xml .

# 2. Copy TẤT CẢ 5 file POM con (Rất quan trọng)
COPY user-service/pom.xml ./user-service/
COPY vehicle-service/pom.xml ./vehicle-service/
COPY part-service/pom.xml ./part-service/
COPY campaign-service/pom.xml ./campaign-service/
COPY warranty-service/pom.xml ./warranty-service/

# 3. Tải dependencies (chỉ cho module này)
RUN mvn -pl ${MODULE_NAME} -am dependency:go-offline

# 4. Copy code nguồn (chỉ cho module này)
COPY ${MODULE_NAME}/src ./${MODULE_NAME}/src

# 5. Build (chỉ module này)
RUN mvn -pl ${MODULE_NAME} -am clean package -DskipTests

# --- GIAI ĐOẠN 2: Run (Dùng JRE 21 Tối ưu) ---
FROM eclipse-temurin:21-jre-alpine
ARG MODULE_NAME
WORKDIR /app
EXPOSE 8080

# 6. Copy file .jar đã build từ Giai đoạn 1
COPY --from=build /app/${MODULE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]