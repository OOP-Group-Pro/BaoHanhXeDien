package com.oem.evwarranty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AffectedVehicleCreateRequest {
    @NotNull
    private Integer campaignId;

    @NotBlank
    private String vehicleVin;

    // optional: assignedServiceCenterId ngay khi tạo
    private Integer assignedServiceCenterId;

    // getter/setter ...


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

    public Integer getAssignedServiceCenterId() {
        return assignedServiceCenterId;
    }

    public void setAssignedServiceCenterId(Integer assignedServiceCenterId) {
        this.assignedServiceCenterId = assignedServiceCenterId;
    }
}
