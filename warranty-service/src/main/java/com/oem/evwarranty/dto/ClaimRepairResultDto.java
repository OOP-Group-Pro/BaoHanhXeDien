package com.oem.evwarranty.dto;

import com.oem.evwarranty.model.utils.SerialUpdateDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/*
        * DTO chứa kết quả sửa chữa, dùng để cập nhật số Serial mới/cũ
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClaimRepairResultDto {
    private Long technicianId; // ID Kỹ thuật viên hoàn thành
    private String finalNotes; // Ghi chú kết quả sửa chữa

    // Map chứa {Mã phụ tùng yêu cầu (partnumber : Số Seri mới được lắp (số serial của phụ tùng mới) }
    // Logic của Service sẽ dùng thông tin này để cập nhật ClaimPartDetail
    private Map<String, SerialUpdateDetail> serialUpdates;
}



