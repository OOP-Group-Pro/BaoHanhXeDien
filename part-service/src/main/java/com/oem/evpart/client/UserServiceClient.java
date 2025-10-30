
package com.oem.evpart.client;

import com.oem.evpart.client.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "user-service", url = "http://localhost:8001")

public interface UserServiceClient {

    @GetMapping("/api/v1/allocations/service-center/{id}")
    UserResponseDTO getServiceCenter(@PathVariable Long id);

    @GetMapping("/api/v1/users/{id}")
    UserResponseDTO getUserById(@PathVariable("id") Long id);
}