package com.oem.evwarranty.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRequestDto {
    private Long technicianId;
    private String approvalNotes;
}
