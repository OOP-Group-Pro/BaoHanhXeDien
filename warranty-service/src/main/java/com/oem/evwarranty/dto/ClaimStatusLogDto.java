package com.oem.evwarranty.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClaimStatusLogDto {
    private String status;
    private String processorName; // Lấy từ User-Service
    private Long processorId;
    private LocalDateTime timestamp;
    private String notes;
}
