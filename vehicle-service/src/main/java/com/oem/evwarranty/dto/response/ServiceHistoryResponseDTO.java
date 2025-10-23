package com.oem.evwarranty.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceHistoryResponseDTO {
    private Long serviceHistoryId;
    private LocalDateTime performedDate;
    private String description;
    private Long vehicleId;
    private TechnicianResponseDTO technician; // Nested DTO for technician details
}