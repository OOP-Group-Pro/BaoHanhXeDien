package com.oem.evcampaign.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CAMPAIGN_PART")
public class CampaignPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_part_id")
    private Long id;

    @Column(name = "part_number", length = 50, nullable = false)
    private String partNumber;

    @Column(name = "part_name", length = 200) // Lưu tên để hiển thị nhanh
    private String partName;

    @Column(name = "quantity_limit")
    private Integer quantityLimit; // Giới hạn số lượng được thay (thường là 1)

    // Quan hệ N-1 với Campaign
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public Integer getQuantityLimit() { return quantityLimit; }
    public void setQuantityLimit(Integer quantityLimit) { this.quantityLimit = quantityLimit; }
    public Campaign getCampaign() { return campaign; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
}