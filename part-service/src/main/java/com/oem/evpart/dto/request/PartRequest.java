package com.oem.evpart.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartRequest {

    @NotBlank(message = "Tên phụ tùng không được để trống")
    private String name;

    private String serialNumber;

    private Double price;

    private String manufacturer;

    private String partType;

    // ⬇️ TRƯỜNG MỚI: ID của chính sách bảo hành áp dụng
    private Long warrantyPolicyId;
}

