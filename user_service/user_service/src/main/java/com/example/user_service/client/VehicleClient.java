package com.example.user_service.client;

import com.example.user_service.dto.VehicleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "vehicle-service", url = "http://localhost:8002/api/vehicles")
public interface VehicleClient {

    @GetMapping("/customer/{customerId}")
    List<VehicleResponse> getVehiclesByCustomerId(@PathVariable("customerId") Long customerId);
}
