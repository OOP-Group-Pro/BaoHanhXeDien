package com.oem.evwarranty.dto.response;

import com.oem.evwarranty.model.enums.CampaignStatus;
import com.oem.evwarranty.model.enums.CampaignType;
import java.time.LocalDateTime;

public class CampaignResponse {
    private Integer id;
    private String code;
    private String title;
    private CampaignType type;
    private CampaignStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // getters/setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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
}
