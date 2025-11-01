package com.oem.evpart.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * DTO này dùng để nhận yêu cầu cấp phát từ Warranty-Service (của Đạt).
 * Nó khớp với DTO mà Đạt muốn gửi.
 */
@Data
public class ClaimAllocationRequest {

    @NotNull(message = "Claim ID không được để trống")
    private Long claimId;

    @NotEmpty(message = "Danh sách phụ tùng không được để trống")
    private List<String> partNumbers; // Giả sử đây là danh sách các partId hoặc partType/SKU

    @NotNull(message = "Service Center ID không được để trống")
    private Long serviceCenterId; // Thống nhất là Integer

    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    private Long quantity; // Cần thêm số lượng, giả sử là 1
}

