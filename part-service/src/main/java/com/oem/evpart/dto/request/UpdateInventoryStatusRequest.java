package com.oem.evpart.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class UpdateInventoryStatusRequest {
    @NotBlank(message = "Status is required")
    private String status; // "Available", "Reserved", "Defective"
}