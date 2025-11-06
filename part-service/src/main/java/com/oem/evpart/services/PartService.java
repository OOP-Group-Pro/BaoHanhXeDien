package com.oem.evpart.services;

import com.oem.evpart.dto.request.PartRequest;
import com.oem.evpart.dto.response.PartResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartService {
    PartResponse createPart(PartRequest partRequest);
    PartResponse getPartById(Long partId);
    Page<PartResponse> getAllParts(Pageable pageable);
    PartResponse updatePart(Long partId, PartRequest partRequest);
    void deletePart(Long partId);
}