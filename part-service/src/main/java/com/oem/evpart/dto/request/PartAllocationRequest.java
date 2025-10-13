package com.oem.evpart.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartAllocationRequest {

    @NotNull(message = "inventoryId là bắt buộc")
    private Long inventoryId;

    @NotNull(message = "serviceCenterId là bắt buộc")
    private Long serviceCenterId;

    @NotNull(message = "Số lượng phân bổ không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer allocatedQty;
}

