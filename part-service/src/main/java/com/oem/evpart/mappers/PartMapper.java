package com.oem.evpart.mappers;

import com.oem.evpart.dto.request.PartRequest;
import com.oem.evpart.dto.response.PartResponse;
import com.oem.evpart.models.Part;
import com.oem.evpart.models.WarrantyPolicy;
import com.oem.evpart.repositories.WarrantyPolicyRepository; // Import Repo
import lombok.RequiredArgsConstructor; // Import Lombok
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor // ✅ Tự động inject Repository
public class PartMapper {

    private final WarrantyPolicyRepository policyRepository; // ✅ Dùng để tìm chính sách

    public Part toPart(PartRequest request) {
        return Part.builder()
                .name(request.getName())
                .serialNumber(request.getSerialNumber())
                .price(request.getPrice())
                .manufacturer(request.getManufacturer())
                .partType(request.getPartType())
                .build();
    }

    public PartResponse toPartResponse(Part part) {
        // ✅ LOGIC MỚI: Tìm Policy dựa trên Part ID
        // (Tìm xem có chính sách nào đang gắn với Part này không)
        WarrantyPolicy policy = policyRepository.findByPart_PartId(part.getPartId())
                .stream().findFirst().orElse(null);

        return PartResponse.builder()
                .partId(part.getPartId())
                .name(part.getName())
                .serialNumber(part.getSerialNumber())
                .price(part.getPrice())
                .manufacturer(part.getManufacturer())
                .partType(part.getPartType())

                // ✅ Map thông tin bảo hành vào Response
                .warrantyDurationMonths(policy != null ? policy.getDurationMonths() : 0)
                .warrantyMileageLimit(policy != null ? policy.getMileageLimit() : 0)
                .warrantyConditions(policy != null ? policy.getConditions() : "Không có chính sách")

                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .build();
    }

    public void updatePartFromRequest(Part part, PartRequest request) {
        if (request.getName() != null) part.setName(request.getName());
        if (request.getSerialNumber() != null) part.setSerialNumber(request.getSerialNumber());
        if (request.getPrice() != null) part.setPrice(request.getPrice());
        if (request.getManufacturer() != null) part.setManufacturer(request.getManufacturer());
        if (request.getPartType() != null) part.setPartType(request.getPartType());
    }
}