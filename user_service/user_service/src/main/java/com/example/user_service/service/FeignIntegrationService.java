package com.example.user_service.service;

import com.example.user_service.client.VehicleClient;
import com.example.user_service.client.WarrantyClient;
import com.example.user_service.client.PartClient;
import com.example.user_service.client.CampaignClient;
import com.example.user_service.dto.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FeignIntegrationService {

    private final VehicleClient vehicleClient;
    private final WarrantyClient warrantyClient;
    private final PartClient partClient;
    private final CampaignClient campaignClient;

    public FeignIntegrationService(VehicleClient vehicleClient, WarrantyClient warrantyClient,
                                   PartClient partClient, CampaignClient campaignClient) {
        this.vehicleClient = vehicleClient;
        this.warrantyClient = warrantyClient;
        this.partClient = partClient;
        this.campaignClient = campaignClient;
    }

    public List<VehicleResponse> getVehiclesByCustomer(Long customerId) {
        return vehicleClient.getVehiclesByCustomerId(customerId);
    }

    public WarrantyResponse getWarranty(Long vehicleId) {
        return warrantyClient.getWarrantyByVehicle(vehicleId);
    }

    public List<PartResponse> getParts(Long vehicleId) {
        return partClient.getPartsByVehicle(vehicleId);
    }

    public List<CampaignResponse> getCampaigns(Long vehicleId) {
        return campaignClient.getCampaignsByVehicle(vehicleId);
    }
}
