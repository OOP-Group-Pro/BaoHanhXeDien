package com.oem.evcampaign.repository;

import com.oem.evcampaign.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByCampaignId(Long campaignId, Pageable pageable);
    Page<Notification> findByAffectedId(Long affectedId, Pageable pageable);
}
