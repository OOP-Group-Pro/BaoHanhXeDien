package com.oem.evwarranty.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO cho WarrantyClient
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyResponse {
    private Long id;
    private String status;
    private String expireDate;
    private String description;
}
