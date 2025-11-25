package com.oem.evpart.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartAllocationResponse {

    private Long allocationId;

    // 1. Thông tin Ngữ cảnh
    private String claimCode;       // Mã Claim (quan trọng để đối chiếu)
    private Long serviceCenterId;   // Trạm nhận hàng

    // 2. Thông tin Phụ tùng (Lấy từ Part)
    private Long partId;
    private String partName;        // Tên (VD: Pin Lithium...)
    private String serialNumber;      // SKU (VD: SN-PIN-LFP-50)
    private String partType;        // Loại (VD: PIN)

    // 3. Thông tin Kho xuất (Lấy từ Inventory)
    private Long inventoryId;
    private String sourceLocation;  // Tên kho xuất (VD: "101 - Hà Nội")

    // 4. Trạng thái & Số lượng
    private Long allocatedQty;      // Số lượng yêu cầu
    private Long deliveredQty;      // Số lượng thực tế đã giao
    private String status;          // Trạng thái (PENDING, READY...)

    // 5. Thời gian
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime allocatedDate;
}