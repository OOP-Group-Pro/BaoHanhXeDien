package com.oem.evwarranty.dto.request;

import com.oem.evwarranty.model.enums.AffectedStatus;

public class AffectedVehicleUpdateRequest {
    private Long assignedServiceCenterId;
    private AffectedStatus status; // PENDING/NOTIFIED/SCHEDULED/COMPLETED ...
    // getter/setter ...

    public Long getAssignedServiceCenterId() {
        return assignedServiceCenterId;
    }

    public void setAssignedServiceCenterId(Long assignedServiceCenterId) {
        this.assignedServiceCenterId = assignedServiceCenterId;
    }

    public AffectedStatus getStatus() {
        return status;
    }

    public void setStatus(AffectedStatus status) {
        this.status = status;
    }
}
