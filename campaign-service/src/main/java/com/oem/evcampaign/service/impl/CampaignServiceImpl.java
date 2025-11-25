package com.oem.evcampaign.service.impl;

import com.oem.evcampaign.dto.request.CampaignCreateRequest;
import com.oem.evcampaign.dto.request.CampaignPartRequest;
import com.oem.evcampaign.dto.request.CampaignUpdateRequest;
import com.oem.evcampaign.dto.response.CampaignPartResponse;
import com.oem.evcampaign.dto.response.CampaignResponse;
import com.oem.evcampaign.dto.response.PageCacheDto;
import com.oem.evcampaign.exception.BadRequestException;
import com.oem.evcampaign.exception.NotFoundException;
import com.oem.evcampaign.model.Campaign;
import com.oem.evcampaign.model.CampaignPart;
import com.oem.evcampaign.model.enums.CampaignStatus;
import com.oem.evcampaign.model.enums.CampaignType;
import com.oem.evcampaign.repository.CampaignRepository;
import com.oem.evcampaign.service.CampaignService;
import com.oem.evcampaign.service.mapper.CampaignMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.Hibernate; // <--- QUAN TRỌNG

@Service
@Transactional
@Slf4j // 1. Thêm cái này để dùng log.info
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepo;

    public CampaignServiceImpl(CampaignRepository campaignRepo) {
        this.campaignRepo = campaignRepo;
    }

    @Override
    // Tạo mới -> Xóa cache tìm kiếm
    @CacheEvict(value = "campaign_search", allEntries = true)
    public CampaignResponse create(CampaignCreateRequest req) {
        if (campaignRepo.existsByCode(req.getCode())) {
            throw new BadRequestException("Campaign code already exists");
        }
        if (req.getEndAt().isBefore(req.getStartAt())) {
            throw new BadRequestException("endAt must be after startAt");
        }

        // 1. Map cơ bản
        Campaign c = CampaignMapper.toEntity(req);

        // 2. Xử lý danh sách phụ tùng
        if (req.getParts() != null && !req.getParts().isEmpty()) {
            for (CampaignPartRequest partReq : req.getParts()) {
                CampaignPart cp = new CampaignPart();
                cp.setPartNumber(partReq.getPartNumber());
                cp.setPartName(partReq.getPartName());
                cp.setQuantityLimit(partReq.getQuantity());
                cp.setCampaign(c); // Gán ngược lại
                c.getCampaignParts().add(cp);
            }
        }

        // 3. Lưu và trả về
        return toResponseWithParts(campaignRepo.save(c));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "campaigns", key = "#id"),
            @CacheEvict(value = "campaign_search", allEntries = true)
    })
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
    // Lấy chi tiết chiến dịch (Kèm phụ tùng)
    @Cacheable(value = "campaigns", key = "#id")
    public CampaignResponse get(Long id) {
        Campaign c = campaignRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
        return toResponseWithParts(c);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "campaigns", key = "#id"),
            @CacheEvict(value = "campaign_search", allEntries = true)
    })
    public void delete(Long id) {
        if (!campaignRepo.existsById(id)) throw new NotFoundException("Campaign not found");
        campaignRepo.deleteById(id);
    }

    @Override
    // Cache kết quả tìm kiếm
    @Cacheable(value = "campaign_search", key = "{#code, #status, #type, #pageable.pageNumber}", condition = "#pageable.pageNumber == 0")
    public PageCacheDto<CampaignResponse> search(String code, CampaignStatus status,
                                                 CampaignType type, Pageable pageable) {
        String keyword = (code != null) ? code.trim() : "";

        Page<CampaignResponse> pageResult = campaignRepo.search(keyword, status, type, pageable)
                .map(CampaignMapper::toResponse);

        return PageCacheDto.from(pageResult);
    }

    @Override
    public java.util.List<CampaignResponse> checkEligibility(String vin) {
        // 1. Tìm các chiến dịch thỏa mãn điều kiện (VIN + STATUS)
        java.util.List<Campaign> campaigns = campaignRepo.findActiveCampaignsByVin(
                vin,
                com.oem.evcampaign.model.enums.CampaignStatus.ACTIVE
        );

        log.info("❤❤❤ Tim thay {} chien dich cho VIN {}", campaigns.size(), vin);

        // 2. ÉP TẢI DỮ LIỆU PHỤ TÙNG (Fix lỗi Lazy Loading)
        // Vì đang trong @Transactional, Hibernate vẫn mở kết nối
        campaigns.forEach(c -> {
            Hibernate.initialize(c.getCampaignParts());
        });

        // 3. Map sang DTO
        return campaigns.stream()
                .map(this::toResponseWithParts)
                .collect(java.util.stream.Collectors.toList());
    }

    // ---------------------------------------------------------
    // HÀM HELPER (Private)
    // ---------------------------------------------------------
    private CampaignResponse toResponseWithParts(Campaign entity) {
        // 1. Dùng Mapper cũ để map các trường cơ bản
        CampaignResponse res = CampaignMapper.toResponse(entity);

        // 2. Tự map thủ công danh sách parts
        if (entity.getCampaignParts() != null) {
            res.setParts(entity.getCampaignParts().stream()
                    .map(cp -> new CampaignPartResponse(
                            cp.getId(),
                            cp.getPartNumber(),
                            cp.getPartName(),
                            cp.getQuantityLimit()))
                    .collect(Collectors.toList()));
        }
        return res;
    }
}