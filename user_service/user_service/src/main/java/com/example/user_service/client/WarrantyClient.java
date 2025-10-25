package com.example.user_service.client;

import com.example.user_service.dto.WarrantyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "warranty-service",
        url = "http://localhost:8005/api/warranties",
        configuration = com.example.user_service.config.FeignConfig.class
)
public interface WarrantyClient {

    @GetMapping("/vehicle/{vehicleId}")
    WarrantyResponse getWarrantyByVehicle(@PathVariable("vehicleId") Long vehicleId);
}
