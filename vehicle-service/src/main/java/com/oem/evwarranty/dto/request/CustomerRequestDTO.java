package com.oem.evwarranty.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequestDTO {

    @NotBlank
    private String customerName;

    private String phoneNum; // Có thể không bắt buộc

    @NotBlank
    @Email
    private String email;
}