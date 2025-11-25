package com.oem.evcampaign.service;

import com.oem.evcampaign.dto.request.CampaignCreateRequest;
import com.oem.evcampaign.dto.request.CampaignUpdateRequest;
import com.oem.evcampaign.dto.response.CampaignResponse;
import com.oem.evcampaign.dto.response.PageCacheDto;
import com.oem.evcampaign.model.enums.CampaignStatus;
import com.oem.evcampaign.model.enums.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CampaignService {
    CampaignResponse create(CampaignCreateRequest req);
    CampaignResponse update(Long id, CampaignUpdateRequest req);
    CampaignResponse get(Long id);
    void delete(Long id);
    PageCacheDto<CampaignResponse> search(String code, CampaignStatus status, CampaignType type, Pageable pageable);
    List<CampaignResponse> checkEligibility(String vin);
}
