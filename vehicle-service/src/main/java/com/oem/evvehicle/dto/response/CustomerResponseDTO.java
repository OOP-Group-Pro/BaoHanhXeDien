package com.oem.evvehicle.dto.response;

import lombok.Data;

@Data
public class CustomerResponseDTO {
    private Long customerId;
    private String customerName;
    private String phoneNum;
    private String email;
}