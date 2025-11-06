package com.oem.evpart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyPolicyRequest {

    @NotNull(message = "partId là bắt buộc")
    private Long partId;

    private Integer durationMonths;

    private Integer mileageLimit;

    private String conditions;
}

