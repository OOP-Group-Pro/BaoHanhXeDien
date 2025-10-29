package com.example.user_service.client;

import com.example.user_service.dto.response.CampaignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "campaign-service", url = "http://localhost:8003/api/campaigns")
public interface CampaignClient {

    @GetMapping("/vehicle/{vehicleId}")
    List<CampaignResponse> getCampaignsByVehicle(@PathVariable("vehicleId") Long vehicleId);
}
