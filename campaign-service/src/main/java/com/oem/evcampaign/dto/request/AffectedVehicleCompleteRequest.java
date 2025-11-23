package com.oem.evcampaign.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AffectedVehicleCompleteRequest {
    @NotBlank
    private String note; // outcome/ghi chú khi hoàn tất
    // getter/setter ...

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
