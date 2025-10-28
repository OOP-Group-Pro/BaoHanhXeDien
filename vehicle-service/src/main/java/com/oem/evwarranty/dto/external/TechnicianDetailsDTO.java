package com.oem.evwarranty.dto.external;
import lombok.Data;

@Data
public class TechnicianDetailsDTO {

    // Khớp với "userId"
    private Long userId;

    // Khớp với "fullName"
    private String fullName;

    // SỬA LẠI: Khớp với "serviceCenterId" (kiểu String)
    private Long serviceCenterId;
}