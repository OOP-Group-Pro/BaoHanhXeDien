package com.oem.evcampaign.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AppointmentCompleteRequest {
    @NotBlank
    private String outcome;

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
}
