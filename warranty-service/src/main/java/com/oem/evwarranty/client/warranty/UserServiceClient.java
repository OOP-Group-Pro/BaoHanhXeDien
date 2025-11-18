package com.oem.evwarranty.client.warranty;

import com.oem.evwarranty.client.warranty.fallback.UserServiceClientFallback;
import com.oem.evwarranty.model.utils.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(
        name="user-staff-service",
        url = "${user-staff-service.url}",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {
    // Get scStaffId
    @GetMapping("/api/v1/users/{userId}")
    UserResponseDto getScStaffById(@PathVariable("userId") Long userId);

    @PostMapping("/api/v1/users/details-map")
    Map<Long, UserResponseDto> getUserDetailsMap (@RequestBody List<Long> userId);
}

