package com.oem.evwarranty.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ClaimDto {
    private Long claimId;
    private String claimCode;
    private String vin;
    private String customerName; // Lấy từ Vehicle-Service
    private String currentStatus;
    private LocalDateTime dateCreated;
    private String description;

    // Bao gồm cả lịch sử trạng thái
    private List<ClaimStatusLogDto> statusHistory;
    private String prepairProcedure;
    private List<ClaimPartDetailDto> partList;
}
