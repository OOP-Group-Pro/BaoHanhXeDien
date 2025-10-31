# 1. Môi trường nền: Chọn Java 21 Slim để tối ưu dung lượng
FROM openjdk:21-jdk-slim

# 2. Thiết lập đối số (argument) cho tên file JAR
# Điều này giúp Docker tìm đúng file JAR trong thư mục target/
ARG JAR_FILE=target/*.jar

# 3. Copy file JAR: Chuyển file JAR đã build từ Host (máy tính của bạn) vào Container
COPY ${JAR_FILE} app.jar

# 4. Công khai cổng: Báo cho Docker biết ứng dụng lắng nghe ở cổng 8080 nội bộ
EXPOSE 8080

# 5. Lệnh chạy: Lệnh thực thi ứng dụng khi Container được khởi động
ENTRYPOINT ["java", "-jar", "/app.jar"]