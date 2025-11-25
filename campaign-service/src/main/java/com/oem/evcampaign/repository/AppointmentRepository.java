package com.oem.evcampaign.repository;

import com.oem.evcampaign.model.Appointment;
import com.oem.evcampaign.model.Campaign;
import com.oem.evcampaign.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByCampaignId(Long campaignId, Pageable pageable);
    Page<Appointment> findByAffectedId(Long affectedId, Pageable pageable);
    Page<Appointment> findByCampaignIdAndStatus(Long campaignId, AppointmentStatus status, Pageable pageable);
    Page<Appointment> findByAffected_Id(Long affectedId, Pageable pageable);
}
