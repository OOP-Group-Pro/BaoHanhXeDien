package com.oem.evpart.dto.response;

// SỬA: Import Enum từ Entity (PartAllocation) thay vì định nghĩa lại
import com.oem.evpart.models.PartAllocation.AllocationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO này được trả về cho Giao diện Kỹ thuật viên
 * để hiển thị trạng thái cấp phát phụ tùng cho một Claim.
 * (Dựa trên image_99162a.jpg)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PartAllocationStatusDto {

    // ID của Claim để Giao diện xác nhận
    private String claimCode;

    // Trạng thái cấp phát
    private AllocationStatus status;

    // SỬA: Đã xóa định nghĩa Enum trùng lặp ở đây.
    // DTO sẽ dùng chung Enum từ PartAllocation.java (file bạn đã cung cấp)
}