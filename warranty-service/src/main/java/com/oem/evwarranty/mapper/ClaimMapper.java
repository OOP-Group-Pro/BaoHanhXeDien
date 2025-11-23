package com.oem.evwarranty.mapper;

import com.oem.evwarranty.client.warranty.UserServiceClient;
import com.oem.evwarranty.client.warranty.VehicleServiceClient;
import com.oem.evwarranty.dto.ClaimDto;
import com.oem.evwarranty.dto.ClaimStatusLogDto;
import com.oem.evwarranty.model.ClaimStatusLog;
import com.oem.evwarranty.model.WarrantyClaim;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClaimMapper {
    // Phương thức tĩnh đơn giản để chuyển Entity sang DTO
    // Trong dự án thực tế, bạn nên dùng MapStruct để tự động hóa việc này.
    // Inject User client:

    private static VehicleServiceClient vehicleClient;
    private static UserServiceClient userClient;

    public static ClaimDto mapToClaimDto(WarrantyClaim claim) {
        if (claim == null) return null;

        ClaimDto dto = new ClaimDto();
        dto.setClaimId(claim.getId());
        dto.setClaimCode(claim.getClaimCode());
        dto.setVin(claim.getVin());
        dto.setCurrentStatus(claim.getCurrentStatus().toString());
        dto.setDateCreated(claim.getDateCreated());
        dto.setDescription(claim.getDescription());
        dto.setCurrentStatus(claim.getCurrentStatus().toString());
        // Map lịch sử log
        if (claim.getStatusLogs() != null) {
            dto.setStatusHistory(
                    claim.getStatusLogs().stream()
                            .map(ClaimMapper::mapToLogDto)
                            .toList()
            );
        }
        if (claim.getPartDetails() != null) {
            dto.setPartList(claim.getPartDetails().stream()
                    .map(PartMapper::mapToClaimPartDetailDto)
            .toList());
        }
        return dto;
    }

    public static ClaimStatusLogDto mapToLogDto(ClaimStatusLog log) {
        ClaimStatusLogDto dto = new ClaimStatusLogDto();
        dto.setStatus(log.getStatus().toString());
        dto.setTimestamp(log.getTimestamp());
        dto.setNotes(log.getNotes());
        dto.setProcessorId(log.getProcessorId());
        return dto;
    }
}
