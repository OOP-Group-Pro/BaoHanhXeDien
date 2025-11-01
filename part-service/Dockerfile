# GIAI ĐOẠN 1: Build (Xây dựng)
# Sử dụng một image Maven đầy đủ (với Java 21) để build file .jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# --- Sửa lỗi tìm Parent POM ---
# 1. Sao chép file pom.xml CỦA CHA (từ thư mục gốc)
COPY ../pom.xml .

# 2. Sao chép file pom.xml CỦA CON (part-service)
# (Dấu . đại diện cho thư mục /app)
COPY pom.xml .

# 3. Tải dependency (tối ưu cache)
RUN mvn dependency:go-offline

# 4. Sao chép toàn bộ source code của part-service
COPY src ./src

# 5. Build dự án, bỏ qua test (vì test nên chạy ở pipeline riêng)
RUN mvn clean package -DskipTests

# --- GIAI ĐOẠN 2: Run (Chạy) ---
# Sử dụng một image JRE (Java Runtime) nhỏ gọn hơn
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Lấy file .jar đã được build từ Giai đoạn 1
# (Sử dụng wildcard * để tự động lấy đúng phiên bản)
COPY --from=build /app/target/part-service-*.jar app.jar

# Mở cổng 8004 (port mà service của bạn đang chạy)
EXPOSE 8004

# Lệnh để chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "app.jar"]

