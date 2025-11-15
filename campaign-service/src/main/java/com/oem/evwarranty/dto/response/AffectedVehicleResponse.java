package com.oem.evwarranty.dto.response;

import com.oem.evwarranty.model.enums.AffectedStatus;
import java.time.LocalDateTime;

public class AffectedVehicleResponse {
    private Long affectedId;
    private Long campaignId;
    private String vehicleVin;
    private AffectedStatus status;
    private Long assignedServiceCenterId;
    private LocalDateTime completedAt;
    // getter/setter ...


    public Long getAffectedId() {
        return affectedId;
    }

    public void setAffectedId(Long affectedId) {
        this.affectedId = affectedId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getVehicleVin() {
        return vehicleVin;
    }

    public void setVehicleVin(String vehicleVin) {
        this.vehicleVin = vehicleVin;
    }

    public AffectedStatus getStatus() {
        return status;
    }

    public void setStatus(AffectedStatus status) {
        this.status = status;
    }

    public Long getAssignedServiceCenterId() {
        return assignedServiceCenterId;
    }

    public void setAssignedServiceCenterId(Long assignedServiceCenterId) {
        this.assignedServiceCenterId = assignedServiceCenterId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
