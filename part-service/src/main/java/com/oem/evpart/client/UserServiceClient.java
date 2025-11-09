package com.oem.evpart.client;

import com.oem.evpart.client.dto.ServiceCenterDTO;
import com.oem.evpart.client.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Interface Feign Client
 * Dùng để Part-Service (8004) gọi sang User-Service (8001).
 */
@FeignClient(name = "user-service",
        url = "${user-service.url}",
        fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    /**
     * Dùng cho nghiệp vụ PartAllocationService
     * Gọi sang User-Service để lấy thông tin và xác thực một Trung tâm dịch vụ.
     * @param id ID của Trung tâm dịch vụ.
     * @return ResponseEntity chứa thông tin nếu tìm thấy.
     */
    @GetMapping("/api/v1/service-centers/{id}")
    ResponseEntity<ServiceCenterDTO> getServiceCenterById(@PathVariable("id") Integer id);

    /**
     * Dùng cho nghiệp vụ AuthenticationFilter
     * Lấy thông tin User (username, roles) để xác thực.
     */
    @GetMapping("/users/{id}")
    ResponseEntity <UserResponseDTO> getUserById(@PathVariable("id") Long id);
}

