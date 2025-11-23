package com.oem.evwarranty.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDto {
    private Long claimId;
    private String claimCode;
    private String vin;
    private String customerName; // Lấy từ Vehicle-Service
    @Nullable
    private String technicalName = null;
    private String currentStatus;
    private LocalDateTime dateCreated;
    private String description;

    // Bao gồm cả lịch sử trạng thái
    private List<ClaimStatusLogDto> statusHistory;
    private String prepairProcedure;
    private List<ClaimPartDetailDto> partList;
    private List<AttachedDocumentDto> documents;
}
