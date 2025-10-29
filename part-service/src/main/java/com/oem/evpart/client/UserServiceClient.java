// File: src/main/java/com/oem/evpart/client/UserServiceClient.java
package com.oem.evpart.client;

import com.oem.evpart.client.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "user-service", url = "${feign.client.config.user-service.url}")

public interface UserServiceClient {

    @GetMapping("/api/v1/users/{id}")
    UserResponseDTO getUserById(@PathVariable("id") Long id);
}