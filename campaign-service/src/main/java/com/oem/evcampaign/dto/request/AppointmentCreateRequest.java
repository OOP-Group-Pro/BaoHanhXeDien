package com.oem.evcampaign.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AppointmentCreateRequest {
    @NotNull
    private Long campaignId;
    @NotNull
    private Long affectedId;
    @NotNull
    private LocalDateTime scheduledAt;
    @NotNull
    private Long serviceCenterId;

    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public Long getAffectedId() { return affectedId; }
    public void setAffectedId(Long affectedId) { this.affectedId = affectedId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public Long getServiceCenterId() { return serviceCenterId; }
    public void setServiceCenterId(Long serviceCenterId) { this.serviceCenterId = serviceCenterId; }
}
