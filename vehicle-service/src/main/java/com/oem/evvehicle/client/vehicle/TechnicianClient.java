package com.oem.evvehicle.client.vehicle;

import com.oem.evvehicle.client.vehicle.fallback.TechnicianClientFallback;
import com.oem.evvehicle.dto.external.TechnicianDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Interface này là Feign Client.
 * Nó định nghĩa "hợp đồng" để gọi sang "user-service".
 * Spring sẽ tự động tạo ra một class thực thi interface này.
 */
@FeignClient(name = "user-technician-service" ,
        contextId = "userClient" ,
        fallback = TechnicianClientFallback.class)
public interface TechnicianClient {

    @GetMapping("/api/v1/users/{userId}")
    TechnicianDetailsDTO getTechnicianDetails(@PathVariable("userId") Long userId);
}