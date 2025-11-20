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
@RequiredArgsConstructor // Sử dụng Lombok để tự động inject dependencies
public class PartServiceImpl implements PartService {

    private final PartRepository partRepository;
    private final PartMapper partMapper;

    @Override
    @Transactional
    public PartResponse createPart(PartRequest partRequest) {
        // Có thể thêm logic kiểm tra serial number đã tồn tại chưa
         if(partRepository.existsBySerialNumber(partRequest.getSerialNumber())) {
             throw new RuntimeException("Serial number already exists");
         }
        Part newPart = partMapper.toPart(partRequest);
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
        // Lưu ý: Do có ràng buộc ON DELETE RESTRICT,
        // nếu Part này đang được tham chiếu ở bảng khác, câu lệnh này sẽ lỗi.
        // Cần xử lý logic phức tạp hơn nếu muốn xóa (ví dụ: chỉ cho xóa khi không còn liên kết)
        partRepository.deleteById(partId);
    }

    @Override
    public Map<String, PartResponse> getPartDetailsByNumbers (List<String> partNumbers) {
        List<Part> parts = partRepository.findAllByPartTypeIn(partNumbers);

        Map<String, PartResponse> partDetails = new HashMap<>();
        for (Part part : parts) {
            partDetails.put(part.getPartType(), partMapper.toPartResponse(part));
        }

        return partDetails;
    }
}