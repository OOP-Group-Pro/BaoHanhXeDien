package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.CampaignCreateRequest;
import com.oem.evwarranty.dto.request.CampaignUpdateRequest;
import com.oem.evwarranty.dto.response.CampaignResponse;
import com.oem.evwarranty.model.enums.CampaignStatus;
import com.oem.evwarranty.model.enums.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CampaignService {
    CampaignResponse create(CampaignCreateRequest req);
    CampaignResponse update(Integer id, CampaignUpdateRequest req);
    CampaignResponse get(Integer id);
    void delete(Integer id);
    Page<CampaignResponse> search(String code, CampaignStatus status, CampaignType type, Pageable pageable);
}
