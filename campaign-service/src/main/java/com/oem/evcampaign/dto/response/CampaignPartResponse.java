package com.oem.evcampaign.dto.response;

public class CampaignPartResponse {
    private Long id;
    private String partNumber;
    private String partName;
    private Integer quantityLimit;

    // Constructors (để map cho dễ)
    public CampaignPartResponse() {}
    public CampaignPartResponse(Long id, String partNumber, String partName, Integer quantityLimit) {
        this.id = id;
        this.partNumber = partNumber;
        this.partName = partName;
        this.quantityLimit = quantityLimit;
    }

    // Getters & Setters... (Bạn tự generate nhé, giống các DTO khác)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public Integer getQuantityLimit() { return quantityLimit; }
    public void setQuantityLimit(Integer quantityLimit) { this.quantityLimit = quantityLimit; }
}