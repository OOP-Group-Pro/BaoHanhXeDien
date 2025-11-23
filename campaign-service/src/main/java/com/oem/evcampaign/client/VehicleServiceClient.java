package com.oem.evcampaign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// application.yml: vehicle-service.url: http://localhost:8002/
@FeignClient(name = "vehicle-service", url = "${vehicle-service.url}")
public interface VehicleServiceClient {

    @GetMapping("/api/v1/vehicles/{vin}")
    VehicleSummary getByVin(@PathVariable("vin") String vin);

    // DTO nhỏ gọn đủ xài để verify VIN
    record VehicleSummary(String vin, String modelCode, String color, Integer year) {}
}
