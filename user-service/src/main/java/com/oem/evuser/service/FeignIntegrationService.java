package com.oem.evuser.service;

import com.oem.evuser.client.user.WarrantyClient;
import com.oem.evuser.dto.request.WarrantyRequest;
import com.oem.evuser.dto.response.WarrantyResponse;
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

