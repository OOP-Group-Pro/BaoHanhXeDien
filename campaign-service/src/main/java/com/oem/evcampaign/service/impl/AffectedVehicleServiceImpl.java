package com.oem.evcampaign.service.impl;

import com.oem.evcampaign.dto.request.AffectedVehicleCompleteRequest;
import com.oem.evcampaign.dto.request.AffectedVehicleCreateRequest;
import com.oem.evcampaign.dto.request.AffectedVehicleScheduleRequest;
import com.oem.evcampaign.dto.request.AffectedVehicleUpdateRequest;
import com.oem.evcampaign.dto.request.*;
import com.oem.evcampaign.dto.response.AffectedVehicleResponse;
import com.oem.evcampaign.exception.BadRequestException;
import com.oem.evcampaign.exception.NotFoundException;
import com.oem.evcampaign.model.AffectedVehicle;
import com.oem.evcampaign.model.Campaign;
import com.oem.evcampaign.model.enums.AffectedStatus;
import com.oem.evcampaign.repository.AffectedVehicleRepository;
import com.oem.evcampaign.repository.CampaignRepository;
import com.oem.evcampaign.service.AffectedVehicleService;
import com.oem.evcampaign.service.mapper.AffectedVehicleMapper;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    // Khi thêm xe vào chiến dịch -> Xóa cache tìm kiếm
    @CacheEvict(value = "affected_search", allEntries = true)
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
    @Caching(evict = {
            @CacheEvict(value = "affected_vehicle", key = "#affectedId"),
            @CacheEvict(value = "affected_search", allEntries = true)
    })
    public AffectedVehicleResponse update(Long affectedId, AffectedVehicleUpdateRequest req) {
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
    @Caching(evict = {
            @CacheEvict(value = "affected_vehicle", key = "#affectedId"),
            @CacheEvict(value = "affected_search", allEntries = true)
    })
    public void delete(Long affectedId) {
        if (!avRepo.existsById(affectedId)) throw new NotFoundException("AffectedVehicle not found");
        avRepo.deleteById(affectedId);
    }

    @Override
    @Cacheable(value = "affected_vehicle", key = "#affectedId")
    public AffectedVehicleResponse get(Long affectedId) {
        return avRepo.findById(affectedId)
                .map(AffectedVehicleMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));
    }

    @Override
    // 🚀 CACHE: Tìm kiếm xe bị ảnh hưởng (Rất quan trọng khi check VIN)
    @Cacheable(value = "affected_search", key = "{#campaignId, #vinKeyword, #status, #pageable.pageNumber}")
    public Page<AffectedVehicleResponse> search(Long campaignId, String vinKeyword,
                                                AffectedStatus status, Pageable pageable) {
        vinKeyword = (vinKeyword == null) ? "" : vinKeyword;

        if (status != null) {
            return avRepo.findByCampaignIdAndStatus(campaignId, status, pageable)
                    .map(AffectedVehicleMapper::toResponse);
        }
        if (!vinKeyword.isEmpty()) {
            return avRepo.findByCampaignIdAndVehicleVinContainingIgnoreCase(campaignId, vinKeyword, pageable)
                    .map(AffectedVehicleMapper::toResponse);
        }
        return avRepo.findByCampaignId(campaignId, pageable)
                .map(AffectedVehicleMapper::toResponse);
    }


    @Override
    @CacheEvict(value = "affected_vehicle", key = "#affectedId")
    public AffectedVehicleResponse markNotified(Long affectedId) {
        AffectedVehicle av = avRepo.findById(affectedId)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));
        av.setStatus(AffectedStatus.NOTIFIED);
        return AffectedVehicleMapper.toResponse(av);
    }

    @Override
    @CacheEvict(value = "affected_vehicle", key = "#affectedId")
    public AffectedVehicleResponse schedule(Long affectedId, AffectedVehicleScheduleRequest req) {
        AffectedVehicle av = avRepo.findById(affectedId)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));

        av.setStatus(AffectedStatus.SCHEDULED);
        if (req.getServiceCenterId() != null) {
            av.setAssignedServiceCenterId(req.getServiceCenterId());
        }
        return AffectedVehicleMapper.toResponse(av);
    }


    @Override
    @CacheEvict(value = "affected_vehicle", key = "#affectedId")
    public AffectedVehicleResponse markCompleted(Long affectedId, AffectedVehicleCompleteRequest req) {
        AffectedVehicle av = avRepo.findById(affectedId)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));
        av.setStatus(AffectedStatus.COMPLETED);
        av.setCompletedAt(LocalDateTime.now());
        // nếu muốn lưu note/outcome, thêm field cho entity
        return AffectedVehicleMapper.toResponse(av);
    }

    @Override
    // Cache tìm kiếm cụ thể 1 xe trong 1 chiến dịch
    @Cacheable(value = "affected_vehicle_campaign", key = "#campaignId + '-' + #affectedId")
    public AffectedVehicleResponse getByCampaign(Long campaignId, Long affectedId) {
        AffectedVehicle av = avRepo.findByIdAndCampaignId(affectedId, campaignId)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));
        return AffectedVehicleMapper.toResponse(av);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "affected_vehicle", key = "#affectedId"),
            @CacheEvict(value = "affected_vehicle_campaign", key = "#campaignId + '-' + #affectedId"),
            @CacheEvict(value = "affected_search", allEntries = true)
    })
    public void deleteByCampaign(Long campaignId, Long affectedId) {
        AffectedVehicle av = avRepo.findByIdAndCampaignId(affectedId, campaignId)
                .orElseThrow(() -> new NotFoundException("AffectedVehicle not found"));
        avRepo.delete(av);
    }

}