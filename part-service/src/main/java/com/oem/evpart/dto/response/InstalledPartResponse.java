package com.oem.evpart.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstalledPartResponse {
    private Long installedId;
    private String vehicleVin;
    private Long partId;
    private String partName;
    private String serialNumber;
    private String status;
    private LocalDateTime installDate;
}
