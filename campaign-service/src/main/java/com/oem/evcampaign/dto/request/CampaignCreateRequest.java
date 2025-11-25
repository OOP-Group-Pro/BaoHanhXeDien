package com.oem.evcampaign.dto.request;

import com.oem.evcampaign.model.enums.CampaignType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List; // Nhớ import List

public class CampaignCreateRequest {
    @NotBlank @Size(max = 32)
    private String code;
    @NotBlank @Size(max = 200)
    private String title;
    @NotNull
    private CampaignType type;
    @NotNull
    private LocalDateTime startAt;
    @NotNull
    private LocalDateTime endAt;

    // MỚI: Danh sách phụ tùng
    private List<CampaignPartRequest> parts;

    // getters/setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public CampaignType getType() { return type; }
    public void setType(CampaignType type) { this.type = type; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    // Getter Setter cho parts
    public List<CampaignPartRequest> getParts() { return parts; }
    public void setParts(List<CampaignPartRequest> parts) { this.parts = parts; }
}
