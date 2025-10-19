package com.oem.evwarranty.client;

import com.oem.evwarranty.dto.external.CenterDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service") // Tên của microservice trung tâm
public interface CenterClient {

    @GetMapping("/api/v1/user-service/centers/{centerId}")
    CenterDetailsDTO getCenterDetails(@PathVariable("centerId") Long centerId);
}