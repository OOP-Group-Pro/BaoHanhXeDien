package com.oem.evwarranty.client.fallback;

import com.oem.evwarranty.client.UserServiceClient;
import com.oem.evwarranty.model.utils.UserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {
    @Override
    public UserResponseDto getScStaffById(Long userId) {
        // Trả về DTO an toàn, tránh NullPointerException trong WarrantyService
        log.error("Feign client ERROR: Cannot get sc staff");
        return new UserResponseDto(
                userId,
                "Lỗi hệ thống/User không xác định",
                null
        );
    }
}
