package com.oem.evwarranty.client;

import com.oem.evwarranty.dto.external.TechnicianDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Interface này là Feign Client.
 * Nó định nghĩa "hợp đồng" để gọi sang "user-service".
 * Spring sẽ tự động tạo ra một class thực thi interface này.
 */
@FeignClient(name = "user-service" , contextId = "userClient")
public interface UserClient {

    @GetMapping("/api/users/technician/{technicianId}")
    TechnicianDetailsDTO getTechnicianDetails(@PathVariable("technicianId") Long technicianId);
}