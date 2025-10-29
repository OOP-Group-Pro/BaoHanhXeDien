package com.example.user_service.controller;

import com.example.user_service.dto.response.CampaignResponse;
import com.example.user_service.dto.response.PartResponse;
import com.example.user_service.dto.response.VehicleResponse;
import com.example.user_service.dto.response.WarrantyResponse;
import com.example.user_service.service.FeignIntegrationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class FeignTestController {

    private final FeignIntegrationService feignService;

    public FeignTestController(FeignIntegrationService feignService) {
        this.feignService = feignService;
    }

    @GetMapping("/vehicles/{customerId}")
    public List<VehicleResponse> getVehicles(@PathVariable Long customerId) {
        return feignService.getVehiclesByCustomer(customerId);
    }

    @GetMapping("/warranty/{vehicleId}")
    public WarrantyResponse getWarranty(@PathVariable Long vehicleId) {
        return feignService.getWarranty(vehicleId);
    }

    @GetMapping("/parts/{vehicleId}")
    public List<PartResponse> getParts(@PathVariable Long vehicleId) {
        return feignService.getParts(vehicleId);
    }

    @GetMapping("/campaigns/{vehicleId}")
    public List<CampaignResponse> getCampaigns(@PathVariable Long vehicleId) {
        return feignService.getCampaigns(vehicleId);
    }
}
