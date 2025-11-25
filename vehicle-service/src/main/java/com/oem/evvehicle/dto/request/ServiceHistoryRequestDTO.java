package com.oem.evvehicle.dto.request;

import com.oem.evvehicle.entity.enums.InstallStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceHistoryRequestDTO {
    @NotNull
    private LocalDateTime performedDate;
    @NotBlank
    private String description;
    @NotNull
    private Long vehicleId;
    @NotNull
    private Long technicianId;
    // 🔥 THAY ĐỔI: Xóa List<Long> partIds cũ, dùng list object chi tiết
    private List<PartInstallationInfo> partsToInstall;

    @Data
    public static class PartInstallationInfo {
        @NotNull
        private Long partId;        // ID loại phụ tùng (để trừ kho)

        @NotBlank
        private String serialNumber; // Serial thực tế lắp vào xe

        @NotNull
        private InstallStatus status; // INSTALLED / REPLACED
    }
    @NotNull(message = "Phải nhập số ODO tại thời điểm sửa chữa")
    private Long odometerReading;
}