package com.oem.evcampaign.dto.response;

import com.oem.evcampaign.model.enums.CampaignStatus;
import com.oem.evcampaign.model.enums.CampaignType;
import java.time.LocalDateTime;
import java.util.List;

public class CampaignResponse {
    private Long id;
    private String code;
    private String title;
    private CampaignType type;
    private CampaignStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // MỚI
    private List<CampaignPartResponse> parts;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public CampaignType getType() { return type; }
    public void setType(CampaignType type) { this.type = type; }
    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    public List<CampaignPartResponse> getParts() { return parts; }
    public void setParts(List<CampaignPartResponse> parts) { this.parts = parts; }
}
