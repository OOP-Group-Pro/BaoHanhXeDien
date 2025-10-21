package com.oem.evwarranty.dto.external;

import lombok.Data;

/**
 * DTO này dùng để hứng dữ liệu chi tiết của Kỹ thuật viên
 * khi gọi từ User-Service.
 * Các trường trong file này PHẢI KHỚP với các trường JSON
 * mà User-Service trả về.
 */
@Data
public class TechnicianDetailsDTO {

    // ID của người dùng (từ User-Service)
    private Long userId;

    // Tên đầy đủ
    private String fullName;

    // Email
    private String email;

    // Số điện thoại
    private String phone;

    // Cấp bậc (Level)
    private String level;

    // ID của Trung tâm Dịch vụ mà người này thuộc về
    private Long centerId;

    // Trạng thái (Active, Inactive...)
    private String status;
}