package com.oem.evwarranty.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceHistoryRequestDTO {
    private LocalDateTime performedDate;
    private String description;
    private Long vehicleId;
    private Long technicianId;
    private List<Long> partIds; // Danh sách ID của các linh kiện liên quan
}