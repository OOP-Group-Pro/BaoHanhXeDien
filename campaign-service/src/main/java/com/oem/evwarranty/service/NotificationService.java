package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.NotificationCreateRequest;
import com.oem.evwarranty.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationResponse create(NotificationCreateRequest req);
    NotificationResponse get(Integer id);
    void delete(Integer id);

    Page<NotificationResponse> listByCampaign(Integer campaignId, Pageable pageable);
    Page<NotificationResponse> listByAffected(Integer affectedId, Pageable pageable);
}