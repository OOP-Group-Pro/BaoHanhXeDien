package com.oem.evcampaign.service;

import com.oem.evcampaign.dto.request.NotificationCreateRequest;
import com.oem.evcampaign.dto.response.NotificationResponse;
import com.oem.evcampaign.dto.response.PageCacheDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationResponse create(NotificationCreateRequest req);
    NotificationResponse get(Long id);
    void delete(Long id);

    PageCacheDto<NotificationResponse> listByCampaign(Long campaignId, Pageable pageable);
    PageCacheDto<NotificationResponse> listByAffected(Long affectedId, Pageable pageable);
}