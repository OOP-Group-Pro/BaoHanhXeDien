package com.oem.evwarranty.dto.external;

import lombok.Data;

@Data
public class CenterDetailsDTO {
    private Long centerId;
    private String centerName;
    private String address;
    private String phone;
}