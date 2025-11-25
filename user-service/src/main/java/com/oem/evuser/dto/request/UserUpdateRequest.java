package com.oem.evuser.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    // Người dùng chỉ được sửa thông tin liên lạc
    private String email;
    private String phone;
}