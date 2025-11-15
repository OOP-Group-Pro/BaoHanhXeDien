package com.oem.evwarranty.dto.response;

import com.oem.evwarranty.model.enums.AppointmentStatus;
import java.time.LocalDateTime;

public class AppointmentResponse {
    private Long appointmentId;
    private Long campaignId;
    private Long affectedId;
    private LocalDateTime scheduledAt;
    private Long serviceCenterId;
    private AppointmentStatus status;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public Long getAffectedId() { return affectedId; }
    public void setAffectedId(Long affectedId) { this.affectedId = affectedId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public Long getServiceCenterId() { return serviceCenterId; }
    public void setServiceCenterId(Long serviceCenterId) { this.serviceCenterId = serviceCenterId; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
