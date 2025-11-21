package com.oem.evpart.services.impl;

import com.oem.evpart.dto.request.PartRequest;
import com.oem.evpart.dto.response.PartResponse;
import com.oem.evpart.exceptions.ResourceNotFoundException;
import com.oem.evpart.mappers.PartMapper;
import com.oem.evpart.models.Part;
import com.oem.evpart.repositories.PartRepository;
import com.oem.evpart.services.PartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PartServiceImpl implements PartService {

    private final PartRepository partRepository;
    private final PartMapper partMapper;

    @Override
    @Transactional
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
    public PartResponse getPartById(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + partId));
        return partMapper.toPartResponse(part);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartResponse> getAllParts(Pageable pageable) {
        return partRepository.findAll(pageable).map(partMapper::toPartResponse);
    }

    @Override
    @Transactional
    public PartResponse updatePart(Long partId, PartRequest partRequest) {
        Part existingPart = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + partId));

        partMapper.updatePartFromRequest(existingPart, partRequest);
        Part updatedPart = partRepository.save(existingPart);
        return partMapper.toPartResponse(updatedPart);
    }

    @Override
    @Transactional
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
        List<Part> parts = partRepository.findAllBySerialNumberIn(partNumbers);

        Map<String, PartResponse> partDetails = new HashMap<>();
        for (Part part : parts) {
            // Key là serialNumber, Value là Response (đã có thông tin bảo hành nhờ Mapper)
            partDetails.put(part.getSerialNumber(), partMapper.toPartResponse(part));
        }
        return partDetails;
    }
}