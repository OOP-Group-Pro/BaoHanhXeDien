package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Page<Notification> findByCampaignId(Integer campaignId, Pageable pageable);
    Page<Notification> findByAffectedId(Integer affectedId, Pageable pageable);
}
