package com.oem.evwarranty.client;

import com.oem.evwarranty.model.utils.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="user-service")
public interface UserServiceClient {
    // Get scStaffId
    @GetMapping("/api/v1/users/{userId}")
    UserResponseDto getScStaffById(@PathVariable("userId") Long userId);
}

