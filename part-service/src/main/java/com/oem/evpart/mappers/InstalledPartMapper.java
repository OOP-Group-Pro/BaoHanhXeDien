// File: src/main/java/com/oem/evpart/mappers/InstalledPartMapper.java
package com.oem.evpart.mappers;

import com.oem.evpart.dto.request.InstalledPartRequest;
import com.oem.evpart.dto.response.InstalledPartResponse;
import com.oem.evpart.models.InstalledPart;
import org.springframework.stereotype.Component;

@Component
public class InstalledPartMapper {

    // FIX: This method was missing or named incorrectly.
    public InstalledPart toInstalledPart(InstalledPartRequest request) {
        return InstalledPart.builder()
                .vehicleVin(request.getVehicleVin())
                .serialNumber(request.getSerialNumber())
                .status(InstalledPart.Status.Installed) // Default to 'Installed' enum on creation
                .build();
    }

    // FIX: This method was missing or named incorrectly.
    public InstalledPartResponse toInstalledPartResponse(InstalledPart installedPart) {
        return InstalledPartResponse.builder()
                .installedId(installedPart.getInstalledId())
                .vehicleVin(installedPart.getVehicleVin())
                .partId(installedPart.getPart().getPartId())
                .partName(installedPart.getPart().getName())
                .serialNumber(installedPart.getSerialNumber())
                .installDate(installedPart.getInstallDate())
                .status(installedPart.getStatus().name()) // Convert Enum to String for the response
                .build();
    }
}