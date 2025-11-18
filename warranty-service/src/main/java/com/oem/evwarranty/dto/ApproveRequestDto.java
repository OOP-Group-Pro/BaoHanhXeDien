package com.oem.evwarranty.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveRequestDto {
    private Long technicianId;
    private String approvalNotes;
}
