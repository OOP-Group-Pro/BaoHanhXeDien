package com.oem.evpart.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class UpdateInstallationStatusRequest {
    @NotBlank(message = "Status is required")
    private String status; // "Installed", "Replaced", "Removed"
}