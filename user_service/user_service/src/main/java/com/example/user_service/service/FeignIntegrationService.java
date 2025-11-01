package com.example.user_service.service;

import com.example.user_service.client.WarrantyClient;
import com.example.user_service.dto.request.WarrantyRequest;
import com.example.user_service.dto.response.WarrantyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeignIntegrationService {

    private final WarrantyClient warrantyClient;

    public void createWarranty(WarrantyRequest request) {
        warrantyClient.createWarranty(request);
    }

    public WarrantyResponse getWarrantyById(Long id) {
        return warrantyClient.getWarrantyById(id);
    }
    public WarrantyResponse getWarranty(Long id) {
        return getWarrantyById(id);
    }

}

