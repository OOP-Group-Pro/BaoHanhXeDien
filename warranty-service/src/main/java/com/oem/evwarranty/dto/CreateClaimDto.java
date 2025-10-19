package com.oem.evwarranty.dto;

import com.oem.evwarranty.model.ClaimPartDetail;
import com.oem.evwarranty.model.utils.PartRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
/**
 * DTO chứa dữ liệu tạo một Warranty Claim mới
 */
public class CreateClaimDto {
    private String vin;                     // Số VIN của xe (bắt buộc)
    private String description;             // Mô tả lỗi của xe
    private Long technicianId;              // ID Kỹ thuật viên được chỉ định ban đầu
    private boolean isRecall;               // Có phải Claim Recall không

    // Chi tiết phụ tùng yêu cầu (dùng để tạo ClaimPartDetail)
    private List<PartRequestDto> requestedParts;

    // Metadata tài liệu đính kèm (dùng để tạo AttachedDocument)
    private List<DocumentMetadataDto> attachedDocuments;
}

// Class phụ cho metadata tài liệu
@Getter
@Setter
@AllArgsConstructor
class DocumentMetadataDto {
    private String fileName;
    private String storagePath;
}
