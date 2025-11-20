package com.oem.evvehicle.dto.response;

import com.oem.evvehicle.entity.enums.InstallStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InstalledPartResponseDTO {
    private Long installedId;
    private Long partId;
    // Bổ sung: Mã phụ tùng (Lấy từ bảng Parts danh mục)
    private String partNumber;
    // Bổ sung: Tên phụ tùng (VD: Battery Pack High Voltage)
    private String partName;
    private String serialNumber;
    private LocalDateTime installDate;
    private InstallStatus status;
    private Long vehicleId; // Chỉ cần trả về ID của xe
}