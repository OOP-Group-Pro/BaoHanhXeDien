package com.oem.evwarranty.dto.request;

import com.oem.evwarranty.entity.enums.InstallStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InstalledPartRequestDTO {
    private Long partId;
    private String serialNumber;
    private LocalDateTime installDate;
    private InstallStatus status;
    private Long vehicleId; // ID của xe mà linh kiện được lắp vào
}