package com.oem.evcampaign.dto.request;

import com.oem.evcampaign.model.enums.AppointmentStatus;
import java.time.LocalDateTime;

public class AppointmentUpdateRequest {

    private LocalDateTime scheduledAt;
    private Long serviceCenterId;
    private AppointmentStatus status;

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Long getServiceCenterId() {
        return serviceCenterId;
    }

    public void setServiceCenterId(Long serviceCenterId) {
        this.serviceCenterId = serviceCenterId;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
