package com.oem.evcampaign.model;

import com.oem.evcampaign.model.enums.CampaignStatus;
import com.oem.evcampaign.model.enums.CampaignType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "CAMPAIGN",
        uniqueConstraints = @UniqueConstraint(name = "uk_campaign_code", columnNames = "code")
)
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    private Long id;

    @Column(length = 32, nullable = false)
    private String code;

    @Column(length = 200, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private CampaignType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private CampaignStatus status;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // Relations
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AffectedVehicle> affectedVehicles = new ArrayList<>();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public CampaignType getType() { return type; }
    public void setType(CampaignType type) { this.type = type; }

    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    public List<AffectedVehicle> getAffectedVehicles() { return affectedVehicles; }
    public List<Appointment> getAppointments() { return appointments; }
    public List<Notification> getNotifications() { return notifications; }
}
