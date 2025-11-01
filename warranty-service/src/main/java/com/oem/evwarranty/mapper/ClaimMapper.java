package com.oem.evwarranty.mapper;

import com.oem.evwarranty.client.UserServiceClient;
import com.oem.evwarranty.client.VehicleServiceClient;
import com.oem.evwarranty.dto.ClaimDto;
import com.oem.evwarranty.dto.ClaimStatusLogDto;
import com.oem.evwarranty.model.ClaimStatusLog;
import com.oem.evwarranty.model.WarrantyClaim;
import com.oem.evwarranty.model.utils.UserResponseDto;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

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
