package com.oem.evwarranty.dto.response;

import lombok.Data;

@Data
public class TechnicianResponseDTO {
    private Long technicianId;
    private String technicianName;
    private String technicianLevel;
}