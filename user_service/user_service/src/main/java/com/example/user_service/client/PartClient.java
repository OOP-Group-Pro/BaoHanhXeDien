package com.example.user_service.client;

import com.example.user_service.dto.PartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "part-service", url = "http://localhost:8004/api/parts")
public interface PartClient {

    @GetMapping("/vehicle/{vehicleId}")
    List<PartResponse> getPartsByVehicle(@PathVariable("vehicleId") Long vehicleId);
}
