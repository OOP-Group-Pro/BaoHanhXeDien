package com.oem.evpart.dto.response;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartResponse {
    private Long partId;
    private String name;
    private String serialNumber;
    private Double price;
    private String manufacturer;
    private String partType;

    // ⬇️ THÔNG TIN BẢO HÀNH (Flatten từ Policy ra để Frontend dễ dùng)
    private Long warrantyPolicyId;
    private Integer warrantyDurationMonths;
    private Integer warrantyMileageLimit;
    private String warrantyConditions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

