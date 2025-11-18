package com.oem.evwarranty.model.utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PartResponseDto {
    private Long partId;
    private String name;
    private String serialNumber;
    private String manufacturer;
    private String partType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
