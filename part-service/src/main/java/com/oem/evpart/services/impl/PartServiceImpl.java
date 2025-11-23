package com.oem.evpart.services.impl;

import com.oem.evpart.dto.request.PartRequest;
import com.oem.evpart.dto.response.PageCacheDto;
import com.oem.evpart.dto.response.PartResponse;
import com.oem.evpart.exceptions.ResourceNotFoundException;
import com.oem.evpart.mappers.PartMapper;
import com.oem.evpart.models.Part;
import com.oem.evpart.repositories.PartRepository;
import com.oem.evpart.services.PartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartServiceImpl implements PartService {

    private final PartRepository partRepository;
    private final PartMapper partMapper;

    @Override
    @Transactional
    @CacheEvict(value = "parts_list", allEntries = true)
    public PartResponse createPart(PartRequest partRequest) {
        if(partRepository.existsBySerialNumber(partRequest.getSerialNumber())) {
            throw new RuntimeException("Serial number already exists");
        }
        Part newPart = partMapper.toPart(partRequest);
        newPart.setCreatedAt(java.time.LocalDateTime.now()); // Set thời gian tạo
        Part savedPart = partRepository.save(newPart);
        return partMapper.toPartResponse(savedPart);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "parts", key = "#partId")
    public PartResponse getPartById(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + partId));
        return partMapper.toPartResponse(part);
    }

    @Override
    @Transactional(readOnly = true)
    // Lưu cache dưới dạng PageCacheDto (Serializable & POJO) => An toàn tuyệt đối
    @Cacheable(value = "parts_list", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #keyword")
    public PageCacheDto<PartResponse> getAllParts(String keyword, Pageable pageable) {
        Page<Part> partPage;

        // Logic tìm kiếm giữ nguyên
        if (keyword != null && !keyword.trim().isEmpty()) {
            partPage = partRepository.searchParts(keyword.trim(), pageable);
        } else {
            partPage = partRepository.findAll(pageable);
        }

        // CHUYỂN ĐỔI TỪ PAGE -> PAGECACHEDTO

            return PageCacheDto.from(partPage.map(partMapper::toPartResponse));
        }


        @Override
    @Transactional
    @Caching(
            put = { @CachePut(value = "parts", key = "#partId") },
            evict = { @CacheEvict(value = "parts_list", allEntries = true) }
    )
    public PartResponse updatePart(Long partId, PartRequest partRequest) {
        Part existingPart = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + partId));

        partMapper.updatePartFromRequest(existingPart, partRequest);
        Part updatedPart = partRepository.save(existingPart);
        return partMapper.toPartResponse(updatedPart);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "parts", key = "#partId"),
                    @CacheEvict(value = "parts_list", allEntries = true)
            }
    )
    public void deletePart(Long partId) {
        if (!partRepository.existsById(partId)) {
            throw new ResourceNotFoundException("Part not found with id: " + partId);
        }
        partRepository.deleteById(partId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, PartResponse> getPartDetailsByNumbers(List<String> partNumbers) {
        // ⚠️ LƯU Ý: Nếu partNumbers là danh sách serialNumber (ví dụ PN-123), dùng method này
        // Nếu partNumbers là partType (ví dụ MOTOR), bạn cần sửa lại repository call tương ứng.
        // Ở đây tôi giả định bạn tìm theo serialNumber (SKU)
        //log.info("🤔🤔 getPartDetailsByNumbers received partNumbers : {} ", partNumbers);
        List<Part> parts = partRepository.findAllByPartTypeIn(partNumbers);
        parts.forEach(part -> {
           // log.info("🤔🤔 partRepository.findAllByPartTypeIn : {}", part);
        });

        Map<String, PartResponse> partDetails = new HashMap<>();
        for (Part part : parts) {
            // Key là serialNumber, Value là Response (đã có thông tin bảo hành nhờ Mapper)
            partDetails.put(part.getPartType(), partMapper.toPartResponse(part));
        }
        return partDetails;
    }
}
