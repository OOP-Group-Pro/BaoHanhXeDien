package com.oem.evcampaign.dto.request;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

public class AffectedVehicleScheduleRequest {
    @NotNull
    private LocalDateTime scheduledAt;
    @NotNull
    private Long serviceCenterId; //
    // getter/setter ...

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
