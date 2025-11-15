package com.oem.evwarranty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AffectedVehicleCreateRequest {
    @NotNull
    private Long campaignId;

    @NotBlank
    private String vehicleVin;

    // optional: assignedServiceCenterId ngay khi tạo
    private Long assignedServiceCenterId;

    // getter/setter ...


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

    public Long getAssignedServiceCenterId() {
        return assignedServiceCenterId;
    }

    public void setAssignedServiceCenterId(Long assignedServiceCenterId) {
        this.assignedServiceCenterId = assignedServiceCenterId;
    }
}
