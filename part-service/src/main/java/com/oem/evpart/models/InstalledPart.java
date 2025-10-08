package com.oem.evpart.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "InstalledPart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstalledPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "installed_id")
    private Long installedId;

    @Column(name = "vehicle_vin", nullable = false, length = 50)
    private String vehicleVin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "install_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime installDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Installed','Replaced','Removed') DEFAULT 'Installed'")
    private Status status = Status.Installed;

    public enum Status {
        Installed, Replaced, Removed
    }
}
