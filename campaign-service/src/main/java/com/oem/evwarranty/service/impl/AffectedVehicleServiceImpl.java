package com.oem.evwarranty.service.impl;

import com.oem.evwarranty.dto.request.*;
import com.oem.evwarranty.dto.response.AffectedVehicleResponse;
import com.oem.evwarranty.exception.BadRequestException;
import com.oem.evwarranty.exception.NotFoundException;
import com.oem.evwarranty.model.AffectedVehicle;
import com.oem.evwarranty.model.Campaign;
import com.oem.evwarranty.model.enums.AffectedStatus;
import com.oem.evwarranty.repository.AffectedVehicleRepository;
import com.oem.evwarranty.repository.CampaignRepository;
import com.oem.evwarranty.service.AffectedVehicleService;
import com.oem.evwarranty.service.mapper.AffectedVehicleMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class AffectedVehicleServiceImpl implements AffectedVehicleService {

    private final AffectedVehicleRepository avRepo;
    private final CampaignRepository campaignRepo;

    public AffectedVehicleServiceImpl(AffectedVehicleRepository avRepo,
                                      CampaignRepository campaignRepo) {
        this.avRepo = avRepo;
        this.campaignRepo = campaignRepo;
    }

    @Override
    public AffectedVehicleResponse create(AffectedVehicleCreateRequest req) {
        Campaign campaign = campaignRepo.findById(req.getCampaignId())
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        if (avRepo.existsByCampaignIdAndVehicleVin(req.getCampaignId(), req.getVehicleVin())) {
            throw new BadRequestException("Vehicle already added to campaign");
        }

        AffectedVehicle av = new AffectedVehicle();
        av.setCampaign(campaign);
        av.setVehicleVin(req.getVehicleVin());
        av.setStatus(AffectedStatus.PENDING);
        av.setAssignedServiceCenterId(req.getAssignedServiceCenterId());
        avRepo.save(av);
        return AffectedVehicleMapper.toResponse(av);
    }

    @Override
    public AffectedVehicleResponse update(Integer affectedId, AffectedVehicleUpdateRequest req) {
        AffectedVehicle av = avRepo.findById(affectedId)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));

        if (req.getAssignedServiceCenterId() != null) {
            av.setAssignedServiceCenterId(req.getAssignedServiceCenterId());
        }
        if (req.getStatus() != null) {
            av.setStatus(req.getStatus());
        }
        return AffectedVehicleMapper.toResponse(av);
    }

    @Override
    public void delete(Integer affectedId) {
        if (!avRepo.existsById(affectedId)) throw new NotFoundException("AffectedVehicle not found");
        avRepo.deleteById(affectedId);
    }

    @Override
    public AffectedVehicleResponse get(Integer affectedId) {
        return avRepo.findById(affectedId)
                .map(AffectedVehicleMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));
    }

    @Override
    public Page<AffectedVehicleResponse> search(Integer campaignId, String vinKeyword,
                                                AffectedStatus status, Pageable pageable) {
        vinKeyword = (vinKeyword == null) ? "" : vinKeyword;

        if (status != null) {
            return avRepo.findByCampaignIdAndStatus(campaignId, status, pageable)
                    .map(AffectedVehicleMapper::toResponse);
        }
        return avRepo.findByCampaignIdAndVehicleVinContainingIgnoreCase(
                        campaignId, vinKeyword, pageable)
                .map(AffectedVehicleMapper::toResponse);
    }

    @Override
    public AffectedVehicleResponse markNotified(Integer affectedId) {
        AffectedVehicle av = avRepo.findById(affectedId)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));
        av.setStatus(AffectedStatus.NOTIFIED);
        return AffectedVehicleMapper.toResponse(av);
    }

    @Override
    public AffectedVehicleResponse schedule(Integer affectedId, AffectedVehicleScheduleRequest req) {
        AffectedVehicle av = avRepo.findById(affectedId)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));

        av.setStatus(AffectedStatus.SCHEDULED);
        av.setAssignedServiceCenterId(
                req.getServiceCenterId() == null ? av.getAssignedServiceCenterId() : null /* map nếu INT */
        );
        // Bản thân lịch hẹn chi tiết nằm ở bảng Appointment, ở đây chỉ đổi trạng thái.
        return AffectedVehicleMapper.toResponse(av);
    }

    @Override
    public AffectedVehicleResponse markCompleted(Integer affectedId, AffectedVehicleCompleteRequest req) {
        AffectedVehicle av = avRepo.findById(affectedId)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));
        av.setStatus(AffectedStatus.COMPLETED);
        av.setCompletedAt(LocalDateTime.now());
        // nếu muốn lưu note/outcome, thêm field cho entity
        return AffectedVehicleMapper.toResponse(av);
    }
}
