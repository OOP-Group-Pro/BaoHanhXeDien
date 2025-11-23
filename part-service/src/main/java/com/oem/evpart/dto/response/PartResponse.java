package com.oem.evpart.dto.response;


import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat; // ⬅️ Import cái này

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
    private Integer inventoryQuantity;

    // ⬇️ THÔNG TIN BẢO HÀNH (Flatten từ Policy ra để Frontend dễ dùng)
    private Long warrantyPolicyId;
    private Integer warrantyDurationMonths;
    private Integer warrantyMileageLimit;
    private String warrantyConditions;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}

