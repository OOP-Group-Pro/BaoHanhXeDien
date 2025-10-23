package com.oem.evwarranty.dto.response;

import com.oem.evwarranty.model.enums.AppointmentStatus;
import java.time.LocalDateTime;

public class AppointmentResponse {
    private Integer appointmentId;
    private Integer campaignId;
    private Integer affectedId;
    private LocalDateTime scheduledAt;
    private Integer serviceCenterId;
    private AppointmentStatus status;

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
    public Integer getCampaignId() { return campaignId; }
    public void setCampaignId(Integer campaignId) { this.campaignId = campaignId; }
    public Integer getAffectedId() { return affectedId; }
    public void setAffectedId(Integer affectedId) { this.affectedId = affectedId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public Integer getServiceCenterId() { return serviceCenterId; }
    public void setServiceCenterId(Integer serviceCenterId) { this.serviceCenterId = serviceCenterId; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
