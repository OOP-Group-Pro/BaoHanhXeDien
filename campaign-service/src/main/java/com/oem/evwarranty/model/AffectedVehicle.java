package com.oem.evwarranty.model;

import com.oem.evwarranty.model.enums.AffectedStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AFFECTED_VEHICLE",
        indexes = {
                @Index(name = "idx_aff_vehicle_campaign", columnList = "campaign_id"),
                @Index(name = "idx_aff_vehicle_vin", columnList = "vehicle_vin")
        }
)
public class AffectedVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "affected_id")
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_aff_vehicle_campaign"))
    private Campaign campaign;

    @Column(name = "vehicle_vin", length = 17, nullable = false)
    private String vehicleVin;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private AffectedStatus status;

    @Column(name = "assigned_service_center_id")
    private Integer assignedServiceCenterId; // nullable

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // nullable

    // getters/setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Campaign getCampaign() { return campaign; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }

    public String getVehicleVin() { return vehicleVin; }
    public void setVehicleVin(String vehicleVin) { this.vehicleVin = vehicleVin; }

    public AffectedStatus getStatus() { return status; }
    public void setStatus(AffectedStatus status) { this.status = status; }

    public Integer getAssignedServiceCenterId() { return assignedServiceCenterId; }
    public void setAssignedServiceCenterId(Integer assignedServiceCenterId) { this.assignedServiceCenterId = assignedServiceCenterId; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}