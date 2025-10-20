package com.oem.evpart.dto.response;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartResponse {
    private Long partId;
    private String name;
    private String serialNumber;
    private String manufacturer;
    private String partType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

