package com.oem.evwarranty.dto;
// ⚠️ LƯU Ý: Bên Warranty Service thì đổi package thành com.oem.evwarranty.dto.request

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncServiceHistoryRequest {
    private String vin;                  // Số VIN để tìm xe
    private LocalDateTime performedDate; // Ngày hoàn thành
    private String description;          // Mô tả (VD: Bảo hành theo Claim WC-123)
    private Long odometerReading;        // ODO hiện tại
    private Long technicianId;           // ID Kỹ thuật viên thực hiện

    // Danh sách phụ tùng mới lắp vào
    private List<SyncPartItem> replacedParts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncPartItem {
        private String partNumber;   // Mã loại (VD: PIN)
        private String partName;     // Tên phụ tùng
        private String serialNumber; // Serial MỚI (Replace)
    }
}