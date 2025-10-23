package com.oem.evwarranty.mapper;

import com.oem.evwarranty.model.ClaimPartDetail;
import com.oem.evwarranty.model.WarrantyClaim;
import com.oem.evwarranty.model.utils.PartRequestDto;
import org.springframework.stereotype.Component;

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
}
