package com.oem.evvehicle.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TechnicianRequestDTO {
    @NotBlank
    private String technicianName;
    @NotBlank
    private String technicianLevel;
    private String status;
    // (Chỉ chứa các trường cần thiết để tạo/cập nhật)
}

