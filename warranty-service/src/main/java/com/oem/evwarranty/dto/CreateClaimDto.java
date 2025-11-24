package com.oem.evwarranty.dto;

import com.oem.evwarranty.model.utils.PartRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * DTO chứa dữ liệu tạo một Warranty Claim mới
 */
public class CreateClaimDto {
    private String vin;                     // Số VIN của xe (bắt buộc)
    private String description;             // Mô tả lỗi của xe
    private boolean isRecall;               // Có phải Claim Recall không

    // Chi tiết phụ tùng yêu cầu (dùng để tạo ClaimPartDetail)
    private List<PartRequestDto> requestedParts;
}

// Class phụ cho metadata tài liệu
@Getter
@Setter
@AllArgsConstructor
class DocumentMetadataDto {
    private String fileName;
    private String storagePath;
}
