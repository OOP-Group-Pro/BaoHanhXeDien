package com.oem.evvehicle.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceHistoryResponseDTO {
    private Long serviceHistoryId;
    private LocalDateTime performedDate;
    private String description;
    private String vehicleVin;
    private String technicianName; // Lấy từ UserClient
    private String centerName;     // Lấy từ CenterClient
    // Thêm ODO để hiển thị ra lưới lịch sử
    private Long odometerReading;
    // Thêm trường này
    private List<InstalledPartResponseDTO> parts;

}