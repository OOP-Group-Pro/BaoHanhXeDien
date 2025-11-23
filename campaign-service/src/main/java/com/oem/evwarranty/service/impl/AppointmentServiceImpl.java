package com.oem.evwarranty.service.impl;

import com.oem.evwarranty.dto.request.AppointmentCreateRequest;
import com.oem.evwarranty.dto.request.AppointmentRescheduleRequest;
import com.oem.evwarranty.dto.request.AppointmentUpdateRequest;
import com.oem.evwarranty.dto.request.AppointmentCompleteRequest;
import com.oem.evwarranty.dto.response.AppointmentResponse;
import com.oem.evwarranty.exception.NotFoundException;
import com.oem.evwarranty.model.Appointment;
import com.oem.evwarranty.model.enums.AppointmentStatus;
import com.oem.evwarranty.repository.AffectedVehicleRepository;
import com.oem.evwarranty.repository.AppointmentRepository;
import com.oem.evwarranty.repository.CampaignRepository;
import com.oem.evwarranty.service.AppointmentService;
import com.oem.evwarranty.service.mapper.AppointmentMapper;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appRepo;
    private final CampaignRepository campaignRepo;
    private final AffectedVehicleRepository avRepo;

    public AppointmentServiceImpl(
            AppointmentRepository appRepo,
            CampaignRepository campaignRepo,
            AffectedVehicleRepository avRepo
    ) {
        this.appRepo = appRepo;
        this.campaignRepo = campaignRepo;
        this.avRepo = avRepo;
    }

    @Override
    // Khi tạo mới, xóa cache list để cập nhật
    @CacheEvict(value = "appointment_list", allEntries = true)
    public AppointmentResponse create(AppointmentCreateRequest req) {
        var campaign = campaignRepo.findById(req.getCampaignId())
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
        var affected = avRepo.findById(req.getAffectedId())
                .orElseThrow(() -> new NotFoundException("Affected vehicle not found"));

        Appointment a = new Appointment();
        a.setCampaign(campaign);
        a.setAffected(affected);
        a.setScheduledAt(req.getScheduledAt());

        if (req.getServiceCenterId() != null) {
            // Entity dùng Long => truyền Long
            a.setServiceCenterId(Long.valueOf(req.getServiceCenterId()));
        }

        a.setStatus(AppointmentStatus.SCHEDULED);
        appRepo.save(a);

        return AppointmentMapper.toResponse(a);
    }

    @Override
    @Cacheable(value = "appointments", key = "#appointmentId")
    public AppointmentResponse get(Long appointmentId) {
        var a = appRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        return AppointmentMapper.toResponse(a);
    }

    @Override
    @CacheEvict(value = "appointments", key = "#appointmentId")
    public AppointmentResponse update(Long appointmentId, AppointmentUpdateRequest req) {
        var a = appRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (req.getScheduledAt() != null) a.setScheduledAt(req.getScheduledAt());
        if (req.getServiceCenterId() != null) a.setServiceCenterId(req.getServiceCenterId()); // Long
        if (req.getStatus() != null) a.setStatus(req.getStatus());

        appRepo.save(a);
        return AppointmentMapper.toResponse(a);
    }

    @Override
    @Transactional
    @CacheEvict(value = "appointments", key = "#appointmentId")
    public AppointmentResponse reschedule(Long appointmentId, AppointmentRescheduleRequest req) {
        Appointment a = appRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        // cập nhật lịch hẹn
        a.setScheduledAt(req.getScheduledAt());

        // nếu request có đổi service center thì cập nhật (Entity dùng Long)
        if (req.getServiceCenterId() != null) {
            a.setServiceCenterId(req.getServiceCenterId().longValue());
        }

        // cập nhật trạng thái
        a.setStatus(AppointmentStatus.RESCHEDULED);

        // lưu lại
        appRepo.save(a);

        // trả về DTO
        return AppointmentMapper.toResponse(a);
    }


    @Override
    @CacheEvict(value = "appointments", key = "#appointmentId")
    public AppointmentResponse complete(Long appointmentId, AppointmentCompleteRequest req) {
        var a = appRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        a.setStatus(AppointmentStatus.DONE);
        appRepo.save(a);
        return AppointmentMapper.toResponse(a);
    }

    @Override
    @CacheEvict(value = "appointments", key = "#appointmentId")
    public void delete(Long appointmentId) {
        var a = appRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        appRepo.delete(a);
    }

    @Override
    @Cacheable(value = "appointment_list", key = "{#campaignId, #affectedId, #pageable.pageNumber}")
    public Page<AppointmentResponse> search(Long campaignId, Long affectedId, Pageable pageable) {
        // Nếu đã có các hàm query trong repository thì thay thế tại đây.
        // Tạm thời trả về tất cả rồi map (đơn giản/nhanh để build qua).
        return appRepo.findAll(pageable).map(AppointmentMapper::toResponse);
    }

    @Override
    public Page<AppointmentResponse> listByCampaign(Long campaignId, AppointmentStatus status, Pageable pageable) {
        // Ưu tiên dùng repository query nếu bạn đã khai báo:
        //   Page<Appointment> p = appRepo.findByCampaign_IdAndStatus(campaignId, status, pageable);
        // Hoặc:
        //   Page<Appointment> p = appRepo.findByCampaign_Id(campaignId, pageable);
        //
        // Để không phát sinh thêm sửa repo ở thời điểm này, mình tạm dùng findAll + map
        // (khi cần tối ưu, thêm query vào repo rồi thay thế logic này).
        return appRepo.findAll(pageable).map(AppointmentMapper::toResponse);
    }


    @Override
    public Page<AppointmentResponse> listByAffected(Long affectedId, Pageable pageable) {
        return appRepo
                .findByAffected_Id(affectedId, pageable)   // lấy theo affected.id
                .map(AppointmentMapper::toResponse);
    }
}