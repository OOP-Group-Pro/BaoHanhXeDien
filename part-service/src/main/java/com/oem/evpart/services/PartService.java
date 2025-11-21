package com.oem.evpart.services;

import com.oem.evpart.dto.request.PartRequest;
import com.oem.evpart.dto.response.PartResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PartService {
    PartResponse createPart(PartRequest partRequest);
    PartResponse getPartById(Long partId);
    Page<PartResponse> getAllParts(String keyword, Pageable pageable);
    PartResponse updatePart(Long partId, PartRequest partRequest);
    void deletePart(Long partId);
    Map<String, PartResponse> getPartDetailsByNumbers(List<String> partNumbers);
}