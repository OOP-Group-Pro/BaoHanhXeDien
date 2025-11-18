package com.oem.evwarranty.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {
    @Value("${storage.location}") // Đọc từ application.properties
    private String rootLocation;
    private Path rootLocationPath;

    @PostConstruct // Chạy hàm này ngay sau khi Service được tạo
    public void init() {
        try {
            rootLocationPath = Paths.get(rootLocation);
            if (Files.notExists(rootLocationPath)) {
                Files.createDirectories(rootLocationPath);
                System.out.println("Đã tạo thư mục lưu trữ gốc: " + rootLocationPath.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ", e);
        }
    }

    public String save(MultipartFile file, String subDirectory) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Không thể lưu file rỗng.");
        }

        try {
            // 1. Tạo thư mục con (ví dụ: uploads/claims/CLAIM-CODE-123)
            Path targetDirectory = rootLocationPath.resolve(subDirectory);
            Files.createDirectories(targetDirectory);

            // 2. Lấy tên file (làm sạch để tránh lỗi path traversal)
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "file_" + System.currentTimeMillis();
            }
            // Chuẩn hóa tên file (rất quan trọng)
            String cleanFilename = Paths.get(originalFilename).getFileName().toString();

            // 3. Tạo đường dẫn file đích
            Path destinationFile = targetDirectory.resolve(cleanFilename).normalize();

            // 4. Copy file vào
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 5. Trả về đường dẫn TƯƠNG ĐỐI (để lưu vào DB)
            // Ví dụ: "claims/CLAIM-CODE-123/video.mp4"
            return Paths.get(subDirectory).resolve(cleanFilename).toString();

        } catch (IOException e) {
            throw new RuntimeException("Lưu file thất bại. " + file.getOriginalFilename(), e);
        }
    }

    public Resource load(String filename) {
        try {
            Path file = rootLocationPath.resolve(filename).normalize(); // Thêm normalize()
            Resource resource = new UrlResource(file.toUri());

            System.out.println("DEBUG LOG: Đang tìm file tại: " + file.toAbsolutePath()); // ⬅️ Log quan trọng

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                // Ném lỗi cụ thể hơn để frontend bắt được
                throw new RuntimeException("File không tồn tại hoặc không đọc được: " + file.toAbsolutePath());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Lỗi đường dẫn file: " + filename, e);
        }
    }
}
