package com.oem.evwarranty.mapper;

import com.oem.evwarranty.dto.ClaimDto;
import com.oem.evwarranty.dto.ClaimStatusLogDto;
import com.oem.evwarranty.model.ClaimStatusLog;
import com.oem.evwarranty.model.WarrantyClaim;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClaimMapper {
    // Phương thức tĩnh đơn giản để chuyển Entity sang DTO
    // Trong dự án thực tế, bạn nên dùng MapStruct để tự động hóa việc này.
    public static ClaimDto mapToClaimDto(WarrantyClaim claim) {
        if (claim == null) return null;

        ClaimDto dto = new ClaimDto();
        dto.setClaimCode(claim.getClaimCode());
        dto.setVin(claim.getVin());
        dto.setCurrentStatus(claim.getCurrentStatus().toString());
        // Lấy tên khách hàng từ Client (Logic này nên nằm trong Service/Controller)
        // dto.setCustomerName(vehicleClient.getCustomerNameByVin(claim.getVin()));
        // ... (thiết lập các thuộc tính khác)

        // Map lịch sử log
        if (claim.getStatusLogs() != null) {
            dto.setStatusHistory(
                    claim.getStatusLogs().stream()
                            .map(ClaimMapper::mapToLogDto)
                            .toList()
            );
        }
        return dto;
    }

    public static ClaimStatusLogDto mapToLogDto(ClaimStatusLog log) {
        ClaimStatusLogDto dto = new ClaimStatusLogDto();
        dto.setStatus(log.getStatus().toString());
        dto.setTimestamp(log.getTimestamp());
        dto.setNotes(log.getNotes());
        // ... (Lấy Processor Name từ UserServiceClient)
        return dto;
    }
}
