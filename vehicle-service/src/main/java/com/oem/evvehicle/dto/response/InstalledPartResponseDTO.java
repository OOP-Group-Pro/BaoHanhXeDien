package com.oem.evvehicle.dto.response;

import com.oem.evvehicle.entity.enums.InstallStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InstalledPartResponseDTO {
    private Long installedId;
    private Long partId;
    private String serialNumber;
    private LocalDateTime installDate;
    private InstallStatus status;
    private Long vehicleId; // Chỉ cần trả về ID của xe
}