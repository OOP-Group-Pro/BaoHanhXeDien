package com.oem.evwarranty.dto.request;

import java.util.UUID;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

public class AffectedVehicleScheduleRequest {
    @NotNull
    private LocalDateTime scheduledAt;
    @NotNull
    private Integer serviceCenterId; //
    // getter/setter ...

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Integer getServiceCenterId() {
        return serviceCenterId;
    }

    public void setServiceCenterId(Integer serviceCenterId) {
        this.serviceCenterId = serviceCenterId;
    }
}
