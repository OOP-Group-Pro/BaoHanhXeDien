package com.oem.evcampaign.service.mapper;

import com.oem.evcampaign.dto.request.CampaignCreateRequest;
import com.oem.evcampaign.dto.response.CampaignResponse;
import com.oem.evcampaign.model.Campaign;
import com.oem.evcampaign.model.enums.CampaignStatus;

public final class CampaignMapper {
    private CampaignMapper() {}

    public static Campaign toEntity(CampaignCreateRequest req) {
        Campaign c = new Campaign();
        c.setCode(req.getCode());
        c.setTitle(req.getTitle());
        c.setType(req.getType());
        c.setStartAt(req.getStartAt());
        c.setEndAt(req.getEndAt());
        c.setStatus(CampaignStatus.DRAFT);
        return c;
    }

    public static CampaignResponse toResponse(Campaign c) {
        CampaignResponse res = new CampaignResponse();
        res.setId(c.getId());
        res.setCode(c.getCode());
        res.setTitle(c.getTitle());
        res.setType(c.getType());
        res.setStatus(c.getStatus());
        res.setStartAt(c.getStartAt());
        res.setEndAt(c.getEndAt());
        return res;
    }
}
