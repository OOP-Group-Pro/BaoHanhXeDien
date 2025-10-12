package com.oem.evwarranty.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AppointmentCreateRequest {
    @NotNull
    private Integer campaignId;
    @NotNull
    private Integer affectedId;
    @NotNull
    private LocalDateTime scheduledAt;
    @NotNull
    private Integer serviceCenterId;

    public Integer getCampaignId() { return campaignId; }
    public void setCampaignId(Integer campaignId) { this.campaignId = campaignId; }
    public Integer getAffectedId() { return affectedId; }
    public void setAffectedId(Integer affectedId) { this.affectedId = affectedId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public Integer getServiceCenterId() { return serviceCenterId; }
    public void setServiceCenterId(Integer serviceCenterId) { this.serviceCenterId = serviceCenterId; }
}
