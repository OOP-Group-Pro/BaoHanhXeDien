package com.oem.evwarranty.service.mapper;

import com.oem.evwarranty.dto.response.AppointmentResponse;
import com.oem.evwarranty.model.Appointment;

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
