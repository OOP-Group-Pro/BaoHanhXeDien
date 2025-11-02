# GIAI ĐOẠN 1: Build (Xây dựng)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# --- Sửa lỗi tìm Parent POM ---
# 1. Sao chép file pom.xml CỦA CHA (nằm ở thư mục gốc của Context Build)
COPY pom.xml .

# 2. Sao chép file pom.xml CỦA CON (warranty-service/pom.xml)
COPY warranty-service/pom.xml .

# 3. Tải dependency (tối ưu cache)
RUN mvn dependency:go-offline

# 4. Sao chép toàn bộ source code của warranty-service
COPY warranty-service/src ./src # Đường dẫn mới

# 5. Build dự án
RUN mvn clean package -DskipTests

# --- GIAI ĐOẠN 2: Run (Chạy) ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Lấy file .jar đã được build từ Giai đoạn 1
COPY --from=build /app/target/warranty-service-*.jar app.jar

# Mở cổng 8004 (port mà service của bạn đang chạy)
EXPOSE 8080

# Lệnh để chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "app.jar"]



# 4. Công khai cổng: Báo cho Docker biết ứng dụng lắng nghe ở cổng 8080 nội bộ
EXPOSE 8080

# 5. Lệnh chạy: Lệnh thực thi ứng dụng khi Container được khởi động
ENTRYPOINT ["java", "-jar", "/app.jar"]