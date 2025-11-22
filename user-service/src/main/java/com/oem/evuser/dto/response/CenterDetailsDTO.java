package com.oem.evuser.dto.response;

public class CenterDetailsDTO {
    private Long centerId;
    private String centerName;
    private String centerAddress;

    // Constructor
    public CenterDetailsDTO(Long centerId, String centerName, String centerAddress) {
        this.centerId = centerId;
        this.centerName = centerName;
        this.centerAddress = centerAddress;
    }

    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getCenterAddress() {
        return centerAddress;
    }

    public void setCenterAddress(String centerAddress) {
        this.centerAddress = centerAddress;
    }
}
