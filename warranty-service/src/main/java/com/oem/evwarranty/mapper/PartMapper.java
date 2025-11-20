package com.oem.evwarranty.mapper;

import com.oem.evwarranty.dto.ClaimPartDetailDto;
import com.oem.evwarranty.model.ClaimPartDetail;
import com.oem.evwarranty.model.WarrantyClaim;
import com.oem.evwarranty.model.utils.PartRequestDto;

public class PartMapper {
    public static ClaimPartDetail mapToPartDetail(PartRequestDto partRequestDto, WarrantyClaim warrantyClaim){
        return ClaimPartDetail.builder()
                .claim(warrantyClaim)
                .partNumber(partRequestDto.getPartNumber())
                .quantityRequired(partRequestDto.getQuantity())
                .serialNumberReplace(null)
                .serialNumberDefective(null)
                .isApproved(false)
                .build();
    }

    public static ClaimPartDetailDto mapToClaimPartDetailDto(ClaimPartDetail claimPartDetail){
        return ClaimPartDetailDto.builder()
                .partNumber(claimPartDetail.getPartNumber())
                .partName(claimPartDetail.getPartName())
                .isApproved(claimPartDetail.getIsApproved())
                .quantityRequired(claimPartDetail.getQuantityRequired())
                .serialNumberDefective(claimPartDetail.getSerialNumberDefective())
                .serialNumberReplace(claimPartDetail.getSerialNumberReplace())
                .build();
    }

    public static ClaimPartDetail mapRequestToClaimPartDetail (PartRequestDto request) {
        return ClaimPartDetail.builder()
                .partNumber(request.getPartNumber())
                .partName(request.getPartName())
                .quantityRequired(request.getQuantity())
                .isApproved(false)
                .build();
    }
}
