package com.oem.evwarranty.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceHistoryResponseDTO {
    private Long serviceHistoryId;
    private LocalDateTime performedDate;
    private String description;
    private Long vehicleId;
    private String technicianName; // Lấy từ UserClient
    private String centerName;     // Lấy từ CenterClient
}