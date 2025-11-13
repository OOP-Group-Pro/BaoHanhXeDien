package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.NotificationCreateRequest;
import com.oem.evwarranty.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationResponse create(NotificationCreateRequest req);
    NotificationResponse get(Long id);
    void delete(Long id);

    Page<NotificationResponse> listByCampaign(Long campaignId, Pageable pageable);
    Page<NotificationResponse> listByAffected(Long affectedId, Pageable pageable);
}