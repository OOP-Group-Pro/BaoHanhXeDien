package com.oem.evcampaign.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AffectedVehicleCompleteRequest {
    @NotBlank
    private String note; // outcome/ghi chú khi hoàn tất
    // getter/setter ...

    public void setNote(String note) {
        this.note = note;
    }
}
