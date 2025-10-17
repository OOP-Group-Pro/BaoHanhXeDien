package com.oem.evwarranty.dto.response;

import com.oem.evwarranty.model.enums.AffectedStatus;
import java.time.LocalDateTime;

public class AffectedVehicleResponse {
    private Integer affectedId;
    private Integer campaignId;
    private String vehicleVin;
    private AffectedStatus status;
    private Integer assignedServiceCenterId;
    private LocalDateTime completedAt;
    // getter/setter ...


    public Integer getAffectedId() {
        return affectedId;
    }

    public void setAffectedId(Integer affectedId) {
        this.affectedId = affectedId;
    }

    public Integer getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Integer campaignId) {
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

    public Integer getAssignedServiceCenterId() {
        return assignedServiceCenterId;
    }

    public void setAssignedServiceCenterId(Integer assignedServiceCenterId) {
        this.assignedServiceCenterId = assignedServiceCenterId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
