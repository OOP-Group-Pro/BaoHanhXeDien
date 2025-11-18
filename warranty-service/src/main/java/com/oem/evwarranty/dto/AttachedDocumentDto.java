package com.oem.evwarranty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AttachedDocumentDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String url; // Đường dẫn API để tải
}
