package com.oem.evcampaign.service.impl;

import com.oem.evcampaign.dto.response.PageCacheDto;
import com.oem.evcampaign.model.enums.CampaignStatus;
import com.oem.evcampaign.model.enums.CampaignType;
import com.oem.evcampaign.dto.request.CampaignCreateRequest;
import com.oem.evcampaign.dto.request.CampaignUpdateRequest;
import com.oem.evcampaign.dto.response.CampaignResponse;
import com.oem.evcampaign.exception.BadRequestException;
import com.oem.evcampaign.exception.NotFoundException;
import com.oem.evcampaign.model.Campaign;
import com.oem.evcampaign.repository.CampaignRepository;
import com.oem.evcampaign.service.CampaignService;
import com.oem.evcampaign.service.mapper.CampaignMapper;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    // Tạo mới -> Xóa cache tìm kiếm để user thấy dữ liệu mới nhất
    @CacheEvict(value = "campaigns_search", allEntries = true)
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
    // Cập nhật -> Xóa/Update cache chi tiết + Xóa cache tìm kiếm
    @Caching(evict = {
            @CacheEvict(value = "campaigns", key = "#id"),
            @CacheEvict(value = "campaigns_search", allEntries = true)
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
    // 🚀 CACHE: Lấy chi tiết chiến dịch (Rất hay dùng)
    @Cacheable(value = "campaigns", key = "#id")
    public CampaignResponse get(Long id) {
        Campaign c = campaignRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
        return CampaignMapper.toResponse(c);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "campaigns", key = "#id"),
            @CacheEvict(value = "campaigns_search", allEntries = true)
    })
    public void delete(Long id) {
        if (!campaignRepo.existsById(id)) throw new NotFoundException("Campaign not found");
        campaignRepo.deleteById(id);
    }

    @Override
    // 🚀 CACHE: Tìm kiếm chiến dịch (Cache theo tham số tìm kiếm)
    @Cacheable(value = "campaigns_search", key = "{#code, #status, #type, #pageable.pageNumber, #pageable.pageSize}")
    public PageCacheDto<CampaignResponse> search(String code, CampaignStatus status,
                                                 CampaignType type,
                                                 Pageable pageable) {

        // Xử lý keyword: nếu null thì truyền rỗng để query vẫn chạy đúng
        String keyword = (code != null) ? code.trim() : "";

        if (status != null && type != null) {
            return PageCacheDto.from(campaignRepo
                    .findByCodeContainingIgnoreCaseAndStatusAndType(keyword, status, type, pageable)
                    .map(CampaignMapper::toResponse));
        }

        // 1. Gọi hàm search và map sang Response (Kết quả là Page của Spring)
        Page<CampaignResponse> pageResult = campaignRepo.search(keyword, status, type, pageable)
                .map(CampaignMapper::toResponse);

        // 2. Đóng gói Page vào PageCacheDto để trả về (Đây là bước fix lỗi)
        return PageCacheDto.from(pageResult);
    }
}