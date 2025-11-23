package com.oem.evcampaign.dto.request;

import jakarta.validation.constraints.NotNull;

public class NotificationCreateRequest {
    @NotNull
    private Long campaignId;
    @NotNull
    private Long affectedId;
    @NotNull
    private String channel; // EMAIL/SMS/…
    @NotNull
    private String status;  // SENT/FAILED/PENDING…

    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public Long getAffectedId() { return affectedId; }
    public void setAffectedId(Long affectedId) { this.affectedId = affectedId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
