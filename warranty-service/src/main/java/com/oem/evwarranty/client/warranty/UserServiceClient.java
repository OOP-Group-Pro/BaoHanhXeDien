package com.oem.evwarranty.client.warranty;

import com.oem.evwarranty.client.warranty.fallback.UserServiceClientFallback;
import com.oem.evwarranty.model.utils.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name="user-staff-service",
        url = "${user-staff-service.url}",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {
    // Get scStaffId
    @GetMapping("/api/v1/users/{userId}")
    UserResponseDto getScStaffById(@PathVariable("userId") Long userId);
}

