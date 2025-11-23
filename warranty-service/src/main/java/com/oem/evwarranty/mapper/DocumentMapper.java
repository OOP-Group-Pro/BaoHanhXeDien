package com.oem.evwarranty.mapper;

import com.oem.evwarranty.dto.AttachedDocumentDto;
import com.oem.evwarranty.model.AttachedDocument;

public class DocumentMapper {
    public static AttachedDocumentDto mapToAttachedDocumentDto(AttachedDocument document) {
        return AttachedDocumentDto.builder()
                .id(document.getId())
                .url("/claims/download-file/" + document.getId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .build();
    }
}
