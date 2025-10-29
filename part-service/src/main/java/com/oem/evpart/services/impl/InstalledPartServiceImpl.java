// File: InstalledPartServiceImpl.java
package com.oem.evpart.services.impl;

import com.oem.evpart.dto.request.InstalledPartRequest; // <--- ĐẢM BẢO IMPORT ĐÚNG
import com.oem.evpart.dto.response.InstalledPartResponse;
// ... các import khác ...
import com.oem.evpart.exceptions.ResourceNotFoundException;
import com.oem.evpart.mappers.InstalledPartMapper;
import com.oem.evpart.models.InstalledPart;
import com.oem.evpart.models.Part;
import com.oem.evpart.repositories.InstalledPartRepository;
import com.oem.evpart.repositories.PartRepository;
import com.oem.evpart.services.InstalledPartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstalledPartServiceImpl implements InstalledPartService {

    private final InstalledPartRepository installedPartRepository;
    private final PartRepository partRepository;
    private final InstalledPartMapper installedPartMapper;

    @Override // <--- THÊM ANNOTATION NÀY
    @Transactional
    // Chữ ký phương thức phải khớp với Interface
    public InstalledPartResponse recordPartInstallation(InstalledPartRequest request) {
        // 1. Tìm thông tin loại phụ tùng (Part)
        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + request.getPartId()));

        // 2. [Business Logic] Tìm và cập nhật trạng thái của phụ tùng cũ (nếu có)
        installedPartRepository
                .findByVehicleVinAndPart_PartIdAndStatus(request.getVehicleVin(), request.getPartId(), InstalledPart.Status.valueOf("Installed"))
                .ifPresent(oldPart -> {
                    oldPart.setStatus(InstalledPart.Status.valueOf("Replaced"));
                    installedPartRepository.save(oldPart);
                });

        // 3. Tạo bản ghi lắp đặt mới
        InstalledPart newInstalledPart = installedPartMapper.toInstalledPart(request);
        newInstalledPart.setPart(part);

        InstalledPart savedRecord = installedPartRepository.save(newInstalledPart);

        return installedPartMapper.toInstalledPartResponse(savedRecord);
    }

    @Override // Thêm cho các phương thức khác
    @Transactional(readOnly = true)
    public List<InstalledPartResponse> getHistoryByVehicleVin(String vin) {
        return installedPartRepository.findByVehicleVinOrderByInstallDateDesc(vin).stream()
                .map(installedPartMapper::toInstalledPartResponse)
                .collect(Collectors.toList());
    }

    // nếu người dùng gửi lên một chuỗi status không hợp lệ (ví dụ: "Instaled" thay vì "Installed"), InstalledPart.Status.valueOf(status) sẽ gây ra lỗi IllegalArgumentException và làm sập luồng xử lý, trả về lỗi 500 cho client.

    @Override
    @Transactional
    public InstalledPartResponse updateInstallationStatus(Long installedId, String status) {
        InstalledPart installedPart = installedPartRepository.findById(installedId)
                .orElseThrow(() -> new ResourceNotFoundException("Installed part record not found with id: " + installedId));

        try {
            // Chuyển chuỗi thành Enum một cách an toàn
            InstalledPart.Status newStatus = InstalledPart.Status.valueOf(status);
            installedPart.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            // Ném ra một lỗi rõ ràng hơn nếu status không hợp lệ
            throw new IllegalArgumentException("Invalid status value provided: " + status + ". Must be one of 'Installed', 'Replaced', 'Removed'.");
        }

        InstalledPart updatedRecord = installedPartRepository.save(installedPart);
        return installedPartMapper.toInstalledPartResponse(updatedRecord);
    }
}