package com.oem.evvehicle.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.oem.evvehicle.entity.enums.InstallStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InstalledPartRequestDTO {
    @NotNull
    private Long vehicleId;
    @NotNull
    private Long partId;
    @NotBlank
    private String serialNumber;
    @NotNull
    private LocalDateTime installDate;
    @NotBlank
    private InstallStatus status;
}