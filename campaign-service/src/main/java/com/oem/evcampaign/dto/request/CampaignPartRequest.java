package com.oem.evcampaign.dto.request;

public class CampaignPartRequest {
    private String partNumber;
    private String partName;
    private Integer quantity;

    // Getters & Setters
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}