FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Expose port (từ application.yml của bạn)
EXPOSE 8002

# Tên file .jar (lấy từ thư mục target)
# Nó sẽ tìm bất kỳ file .jar nào trong thư mục target
ARG JAR_FILE=target/*.jar

# Copy file .jar (đã build) từ máy của bạn vào image
COPY ${JAR_FILE} app.jar

# Lệnh chạy
ENTRYPOINT ["java", "-jar", "app.jar"]
