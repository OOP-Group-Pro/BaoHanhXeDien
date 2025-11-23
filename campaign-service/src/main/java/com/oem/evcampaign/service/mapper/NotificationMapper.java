package com.oem.evcampaign.service.mapper;

import com.oem.evcampaign.dto.response.NotificationResponse;
import com.oem.evcampaign.model.Notification;

public final class NotificationMapper {
    private NotificationMapper(){}

    public static NotificationResponse toResponse(Notification n) {
        NotificationResponse dto = new NotificationResponse();
        dto.setNotificationId(n.getId());
        dto.setCampaignId(n.getCampaign().getId());
        dto.setAffectedId(n.getAffected().getId());
        dto.setChannel(n.getChannel().name()); // nếu channel là enum
        dto.setStatus(n.getStatus().name());   // nếu status là enum
        dto.setSentAt(n.getSentAt());
        return dto;
    }
}