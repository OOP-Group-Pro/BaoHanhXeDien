package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.AppointmentCreateRequest;
import com.oem.evwarranty.dto.request.AppointmentRescheduleRequest;
import com.oem.evwarranty.dto.request.AppointmentUpdateRequest;
import com.oem.evwarranty.dto.request.AppointmentCompleteRequest;
import com.oem.evwarranty.dto.response.AppointmentResponse;
import com.oem.evwarranty.model.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface    AppointmentService {

    AppointmentResponse create(AppointmentCreateRequest req);

    AppointmentResponse get(Integer appointmentId);

    AppointmentResponse update(Integer appointmentId, AppointmentUpdateRequest req);

    AppointmentResponse reschedule(Integer appointmentId, AppointmentRescheduleRequest req);

    AppointmentResponse complete(Integer appointmentId, AppointmentCompleteRequest req);

    void delete(Integer appointmentId);

    Page<AppointmentResponse> listByAffected(Integer affectedId, Pageable pageable);

    /** Tìm kiếm chung */
    Page<AppointmentResponse> search(Integer campaignId, Integer affectedId, Pageable pageable);

    /** Liệt kê theo campaign + (tuỳ chọn) status */
    Page<AppointmentResponse> listByCampaign(Integer campaignId, AppointmentStatus status, Pageable pageable);
}
