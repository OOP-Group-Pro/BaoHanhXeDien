package com.oem.evwarranty.service.impl;

import com.oem.evwarranty.dto.request.CampaignCreateRequest;
import com.oem.evwarranty.dto.request.CampaignUpdateRequest;
import com.oem.evwarranty.dto.response.CampaignResponse;
import com.oem.evwarranty.exception.BadRequestException;
import com.oem.evwarranty.exception.NotFoundException;
import com.oem.evwarranty.model.Campaign;
import com.oem.evwarranty.repository.CampaignRepository;
import com.oem.evwarranty.service.CampaignService;
import com.oem.evwarranty.service.mapper.CampaignMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepo;

    public CampaignServiceImpl(CampaignRepository campaignRepo) {
        this.campaignRepo = campaignRepo;
    }

    @Override
    public CampaignResponse create(CampaignCreateRequest req) {
        if (campaignRepo.existsByCode(req.getCode())) {
            throw new BadRequestException("Campaign code already exists");
        }
        if (req.getEndAt().isBefore(req.getStartAt())) {
            throw new BadRequestException("endAt must be after startAt");
        }
        Campaign c = CampaignMapper.toEntity(req);
        return CampaignMapper.toResponse(campaignRepo.save(c));
    }

    @Override
    public CampaignResponse update(Long id, CampaignUpdateRequest req) {
        Campaign c = campaignRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        if (req.getTitle() != null) c.setTitle(req.getTitle());
        if (req.getType() != null) c.setType(req.getType());
        if (req.getStatus() != null) c.setStatus(req.getStatus());
        if (req.getStartAt() != null) c.setStartAt(req.getStartAt());
        if (req.getEndAt() != null) {
            if (c.getStartAt() != null && req.getEndAt().isBefore(c.getStartAt()))
                throw new BadRequestException("endAt must be after startAt");
            c.setEndAt(req.getEndAt());
        }
        return CampaignMapper.toResponse(campaignRepo.save(c));
    }

    @Override
    public CampaignResponse get(Long id) {
        Campaign c = campaignRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
        return CampaignMapper.toResponse(c);
    }

    @Override
    public void delete(Long id) {
        if (!campaignRepo.existsById(id)) throw new NotFoundException("Campaign not found");
        campaignRepo.deleteById(id);
    }

    @Override
    public Page<CampaignResponse> search(String code, com.oem.evwarranty.model.enums.CampaignStatus status,
                                         com.oem.evwarranty.model.enums.CampaignType type,
                                         Pageable pageable) {
        String kw = (code == null) ? "" : code;
        if (status != null && type != null) {
            return campaignRepo
                    .findByCodeContainingIgnoreCaseAndStatusAndType(kw, status, type, pageable)
                    .map(CampaignMapper::toResponse);
        }
        return campaignRepo.findByCodeContainingIgnoreCase(kw, pageable)
                .map(CampaignMapper::toResponse);
    }
}
