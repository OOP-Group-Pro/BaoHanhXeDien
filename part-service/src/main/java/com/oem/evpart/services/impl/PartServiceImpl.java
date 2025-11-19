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
        if (partRepository.existsBySerialNumber(partRequest.getSerialNumber())) {
            log.warn("Attempt to create duplicate serialNumber: {}", partRequest.getSerialNumber());
            throw new RuntimeException("Serial number already exists");
        }
        Part newPart = partMapper.toPart(partRequest);
        Part savedPart = partRepository.save(newPart);
        log.info("Created new Part with ID: {}", savedPart.getPartId());
        return partMapper.toPartResponse(savedPart);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "parts", key = "#partId")
    public PartResponse getPartById(Long partId) {
        log.info(">>> Getting Part from Database for ID: {}", partId);
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + partId));
        return partMapper.toPartResponse(part);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "parts_list", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PageCacheDto<PartResponse> getAllParts(Pageable pageable) {
        Page<PartResponse> page = partRepository.findAll(pageable)
                .map(partMapper::toPartResponse);
        return new PageCacheDto<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages()
        );
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
        log.info("Updated Part ID {}", partId);
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
            log.warn("Attempt to delete non-existent Part ID {}", partId);
            throw new ResourceNotFoundException("Part not found with id: " + partId);
        }
        partRepository.deleteById(partId);
        log.info("Deleted Part ID {}", partId);
    }

    @Override
    public Map<String, PartResponse> getPartDetailsByNumbers(List<String> partNumbers) {
        List<Part> parts = partRepository.findAllByPartTypeIn(partNumbers);

        Map<String, PartResponse> partDetails = new HashMap<>();
        for (Part part : parts) {
            partDetails.put(part.getSerialNumber(), partMapper.toPartResponse(part));
        }

        return partDetails;
    }
}
