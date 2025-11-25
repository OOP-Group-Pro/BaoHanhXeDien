package com.oem.evpart.mappers;

import com.oem.evpart.dto.request.PartRequest;
import com.oem.evpart.dto.response.PartResponse;
import com.oem.evpart.models.Part;
import com.oem.evpart.models.PartInventory;
import com.oem.evpart.models.WarrantyPolicy;
import com.oem.evpart.repositories.WarrantyPolicyRepository; // Import Repo
import lombok.RequiredArgsConstructor; // Import Lombok
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor // ✅ Tự động inject Repository
@Slf4j
public class PartMapper {

    private final WarrantyPolicyRepository policyRepository; // ✅ Dùng để tìm chính sách

    public Part toPart(PartRequest request) {
        Part part = Part.builder()
                .name(request.getName())
                .serialNumber(request.getSerialNumber())
                .price(request.getPrice())
                .manufacturer(request.getManufacturer())
                .partType(request.getPartType())
                .build();

        // 🔥 FIX: Tìm và gán WarrantyPolicy nếu có ID
        if (request.getWarrantyPolicyId() != null) {
            WarrantyPolicy policy = policyRepository.findById(request.getWarrantyPolicyId())
                    .orElse(null); // Hoặc ném lỗi nếu muốn bắt buộc phải có
            part.setWarrantyPolicy(policy);

            if (policy == null) {
                log.warn("⚠️ Không tìm thấy Warranty Policy với ID: {}", request.getWarrantyPolicyId());
            }
        }

        return part;
    }

    public PartResponse toPartResponse(Part part) {
        WarrantyPolicy policy = part.getWarrantyPolicy();

        // --- LOG DEBUGGING BẮT ĐẦU ---
        int totalQuantity = 0;

        // In ra tên Part đang xử lý
        log.info("🔍 Mapping Part: {} (ID: {})", part.getName(), part.getPartId());

        if (part.getInventories() != null) {
            // In ra số lượng bản ghi inventory tìm thấy
            log.info("   👉 Found Inventory List. Size: {}", part.getInventories().size());

            for (PartInventory inv : part.getInventories()) {
                log.info("      - Kho: {} | Qty: {} | Status: {}",
                        inv.getLocation(), inv.getQuantity(), inv.getStatus());
            }

            totalQuantity = part.getInventories().stream()
                    .filter(inv -> PartInventory.Status.Available.equals(inv.getStatus()))
                    .mapToInt(inv -> inv.getQuantity().intValue())
                    .sum();

            log.info("   ✅ Calculated Total Available: {}", totalQuantity);
        } else {
            log.warn("   ⚠️ Inventory List is NULL! (Kiểm tra lại FetchType.EAGER trong Entity Part)");
        }
        // --- LOG DEBUGGING KẾT THÚC ---

        return PartResponse.builder()
                .partId(part.getPartId())
                .name(part.getName())
                .serialNumber(part.getSerialNumber())
                .price(part.getPrice())
                .manufacturer(part.getManufacturer())
                .partType(part.getPartType())
                .inventoryQuantity(totalQuantity) // Gán số đã tính

                .warrantyPolicyId(policy != null ? policy.getPolicyId() : null)
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

        if (request.getWarrantyPolicyId() != null) {
            WarrantyPolicy policy = policyRepository.findById(request.getWarrantyPolicyId())
                    .orElse(null);
            part.setWarrantyPolicy(policy);
        }
    }
}