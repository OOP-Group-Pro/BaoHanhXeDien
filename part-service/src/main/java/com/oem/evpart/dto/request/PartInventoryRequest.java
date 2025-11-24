package com.oem.evpart.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartInventoryRequest {

    @NotNull(message = "partId là bắt buộc")
    private Long partId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Long quantity;

    private String location;

    private String status; // Available, Reserved, Defective
}

