package com.oem.evwarranty.client.vehicle;

import com.oem.evwarranty.client.vehicle.fallback.CenterClientFallback;
import com.oem.evwarranty.dto.external.CenterDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-center-service" ,
        contextId = "IdOfCenterClient" ,
        fallback = CenterClientFallback.class) // Tên của microservice trung tâm
public interface CenterClient {

    @GetMapping("/api/v1/service-centers/{id}")
    CenterDetailsDTO getCenterDetails(@PathVariable("id") Long centerId);
}