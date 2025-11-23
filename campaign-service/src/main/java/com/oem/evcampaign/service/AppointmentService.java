package com.oem.evcampaign.service;

import com.oem.evcampaign.dto.request.AppointmentCreateRequest;
import com.oem.evcampaign.dto.request.AppointmentRescheduleRequest;
import com.oem.evcampaign.dto.request.AppointmentUpdateRequest;
import com.oem.evcampaign.dto.request.AppointmentCompleteRequest;
import com.oem.evcampaign.dto.response.AppointmentResponse;
import com.oem.evcampaign.model.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface    AppointmentService {

    AppointmentResponse create(AppointmentCreateRequest req);

    AppointmentResponse get(Long appointmentId);

    AppointmentResponse update(Long appointmentId, AppointmentUpdateRequest req);

    AppointmentResponse reschedule(Long appointmentId, AppointmentRescheduleRequest req);

    AppointmentResponse complete(Long appointmentId, AppointmentCompleteRequest req);

    void delete(Long appointmentId);

    Page<AppointmentResponse> listByAffected(Long affectedId, Pageable pageable);

    /** Tìm kiếm chung */
    Page<AppointmentResponse> search(Long campaignId, Long affectedId, Pageable pageable);

    /** Liệt kê theo campaign + (tuỳ chọn) status */
    Page<AppointmentResponse> listByCampaign(Long campaignId, AppointmentStatus status, Pageable pageable);
}
