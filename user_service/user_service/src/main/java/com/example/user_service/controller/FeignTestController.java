package com.example.user_service.controller;

import com.example.user_service.dto.response.WarrantyResponse;
import com.example.user_service.service.FeignIntegrationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class FeignTestController {

    private final FeignIntegrationService feignService;

    public FeignTestController(FeignIntegrationService feignService) {
        this.feignService = feignService;
    }



    @GetMapping("/warranty/{vehicleId}")
    public WarrantyResponse getWarranty(@PathVariable Long vehicleId) {
        return feignService.getWarranty(vehicleId);
    }


}
