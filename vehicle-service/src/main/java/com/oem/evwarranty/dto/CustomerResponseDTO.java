package com.oem.evwarranty.dto;

import lombok.Data;

@Data
public class CustomerResponseDTO {
    private Long customerId;
    private String customerName;
    private String email;
}