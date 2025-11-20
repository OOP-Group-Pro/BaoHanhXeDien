package com.oem.evvehicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "service_history")
public class ServiceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "servicehistory_id")
    private Long serviceHistoryId;

    @Column(name = "performed_date")
    private LocalDateTime performedDate;

    @Column(name = "description")
    private String description;

    // Relationship to Vehicle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Relationship to Technician
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "service_history_parts", // Tên bảng trung gian sẽ được tạo ra
            joinColumns = @JoinColumn(name = "service_history_id"),
            inverseJoinColumns = @JoinColumn(name = "installed_part_id")
    )
    private Set<InstalledPart> partsInvolved; // Danh sách các linh kiện liên quan

    // QUAN TRỌNG: Xe chạy bao nhiêu Km tại thời điểm sửa chữa này?
    @Column(name = "odometer_reading", nullable = false)
    private Long odometerReading;
}
