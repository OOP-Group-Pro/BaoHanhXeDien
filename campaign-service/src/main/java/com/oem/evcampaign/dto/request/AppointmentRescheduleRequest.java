package com.oem.evcampaign.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AppointmentRescheduleRequest {

    @NotNull
    private LocalDateTime scheduledAt;

    // Cho phép đổi TTTD (optional)
    private Long serviceCenterId;

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
}
