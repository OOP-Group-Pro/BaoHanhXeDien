package com.oem.evwarranty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.oem.evwarranty.entity.enums.InstallStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InstalledPartRequestDTO {
    @NotNull
    private Long partId;
    @NotBlank
    private String serialNumber;
    @NotNull
    private LocalDateTime installDate;
    @NotBlank
    private InstallStatus status;
    @NotNull
    private Long vehicleId;
    @NotBlank
    private String location; // Thêm trường này
    // Để biết trừ kho ở đâu// ID của xe mà linh kiện được lắp vào
}