package com.oem.evwarranty.service.mapper;

import com.oem.evwarranty.dto.response.AffectedVehicleResponse;
import com.oem.evwarranty.model.AffectedVehicle;

public final class AffectedVehicleMapper {
    private AffectedVehicleMapper(){}

    public static AffectedVehicleResponse toResponse(AffectedVehicle av) {
        AffectedVehicleResponse dto = new AffectedVehicleResponse();
        dto.setAffectedId(av.getId());
        dto.setCampaignId(av.getCampaign().getId());
        dto.setVehicleVin(av.getVehicleVin());
        dto.setStatus(av.getStatus());
        dto.setAssignedServiceCenterId(av.getAssignedServiceCenterId());
        dto.setCompletedAt(av.getCompletedAt());
        return dto;
    }
}
