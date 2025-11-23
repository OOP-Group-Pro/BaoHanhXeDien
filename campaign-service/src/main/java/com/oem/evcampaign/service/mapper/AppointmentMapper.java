package com.oem.evcampaign.service.mapper;

import com.oem.evcampaign.dto.response.AppointmentResponse;
import com.oem.evcampaign.model.Appointment;

public final class AppointmentMapper {
    private AppointmentMapper() {}

    public static AppointmentResponse toResponse(Appointment a) {
        AppointmentResponse dto = new AppointmentResponse();
        dto.setAppointmentId(a.getId());
        dto.setCampaignId(a.getCampaign().getId());
        dto.setAffectedId(a.getAffected().getId());
        dto.setScheduledAt(a.getScheduledAt());

        dto.setServiceCenterId(a.getServiceCenterId());

        dto.setStatus(a.getStatus());
        return dto;
    }
}
