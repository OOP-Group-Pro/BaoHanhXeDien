package com.oem.evwarranty.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oem.evwarranty.entity.enums.InstallStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "installed_parts")
public class InstalledPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "installed_id")
    private Long installedId;

    @Column(name = "part_id", nullable = false)
    private Long partId; // Giả định đây là ID của một linh kiện từ một bảng khác

    @Column(name = "serial_number", unique = true)
    private String serialNumber;

    @Column(name = "install_date")
    private LocalDateTime installDate;

    @Enumerated(EnumType.STRING) // Lưu trữ Enum dưới dạng chuỗi trong DB
    @Column(name = "status")
    private InstallStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false) // Liên kết tới vehicle_id
    private Vehicle vehicle;

    @ManyToMany(mappedBy = "partsInvolved")
    @JsonIgnore // Rất quan trọng để tránh lỗi lặp vô hạn khi chuyển sang JSON
    private Set<ServiceHistory> serviceHistories;
}