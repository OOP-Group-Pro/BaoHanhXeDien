package com.oem.evwarranty.dto.request;

import jakarta.validation.constraints.NotNull;

public class NotificationCreateRequest {
    @NotNull
    private Integer campaignId;
    @NotNull
    private Integer affectedId;
    @NotNull
    private String channel; // EMAIL/SMS/…
    @NotNull
    private String status;  // SENT/FAILED/PENDING…

    public Integer getCampaignId() { return campaignId; }
    public void setCampaignId(Integer campaignId) { this.campaignId = campaignId; }
    public Integer getAffectedId() { return affectedId; }
    public void setAffectedId(Integer affectedId) { this.affectedId = affectedId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
