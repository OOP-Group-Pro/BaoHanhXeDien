package com.oem.evpart.mappers;
import com.oem.evpart.dto.request.PartRequest;
import com.oem.evpart.dto.response.PartResponse;
import com.oem.evpart.models.Part;
import org.springframework.stereotype.Component;

@Component
public class PartMapper {

    public Part toPart(PartRequest request) {
        return Part.builder()
                .name(request.getName())
                .serialNumber(request.getSerialNumber())
                .manufacturer(request.getManufacturer())
                .partType(request.getPartType())
                .build();
    }

    public PartResponse toPartResponse(Part part) {
        return PartResponse.builder()
                .partId(part.getPartId())
                .name(part.getName())
                .serialNumber(part.getSerialNumber())
                .manufacturer(part.getManufacturer())
                .partType(part.getPartType())
                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .build();
    }

    public void updatePartFromRequest(Part part, PartRequest request) {
        part.setName(request.getName());
        part.setSerialNumber(request.getSerialNumber());
        part.setManufacturer(request.getManufacturer());
        part.setPartType(request.getPartType());
    }
}