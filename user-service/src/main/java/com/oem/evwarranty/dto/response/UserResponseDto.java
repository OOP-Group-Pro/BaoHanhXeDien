package com.oem.evwarranty.dto.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String fullName;
    private Long serviceCenterId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getServiceCenterId() {
        return serviceCenterId;
    }

    public void setServiceCenterId(Long serviceCenterId) {
        this.serviceCenterId = serviceCenterId;
    }
}
