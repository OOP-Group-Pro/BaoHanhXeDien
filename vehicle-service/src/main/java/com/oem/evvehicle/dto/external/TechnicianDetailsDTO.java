package com.oem.evvehicle.dto.external;
import lombok.Data;

@Data
public class TechnicianDetailsDTO {
    private Long userId;
    private String fullName;
    private Long serviceCenterId;
}