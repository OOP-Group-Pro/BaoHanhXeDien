package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.Appointment;
import com.oem.evwarranty.model.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByCampaignId(Long campaignId, Pageable pageable);
    Page<Appointment> findByAffectedId(Long affectedId, Pageable pageable);
    Page<Appointment> findByCampaignIdAndStatus(Long campaignId, AppointmentStatus status, Pageable pageable);
    Page<Appointment> findByAffected_Id(Long affectedId, Pageable pageable);
}
