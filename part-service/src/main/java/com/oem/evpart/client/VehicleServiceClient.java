package com.oem.evpart.client;

import com.oem.evpart.client.dto.VehicleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client để gọi các API từ Vehicle-Service.
 */
@FeignClient(name = "vehicle-service", url = "${feign.client.config.vehicle-service.url}")
public interface VehicleServiceClient {

    /**
     * Gọi sang Vehicle-Service để lấy thông tin và xác thực một xe qua số VIN.
     * Dùng trong InstalledPartService.
     */
    @GetMapping("/api/v1/vehicles/{vin}")
    ResponseEntity<VehicleDTO> getVehicleByVin(@PathVariable("vin") String vin);
}
