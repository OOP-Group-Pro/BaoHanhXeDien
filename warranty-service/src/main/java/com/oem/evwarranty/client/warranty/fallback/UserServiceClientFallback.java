package com.oem.evwarranty.client.warranty.fallback;

import com.oem.evwarranty.client.warranty.UserServiceClient;
import com.oem.evwarranty.model.utils.UserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                null,
                null,
                null

        );
    }

    @Override
    public Map<Long, UserResponseDto> getUserDetailsMap (List<Long> userId) {
        log.error("Feign client ERROR: Cannot get user details");
        return new HashMap<>();
    }
}
