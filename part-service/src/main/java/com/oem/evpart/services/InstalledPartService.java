// File: InstalledPartService.java
package com.oem.evpart.services;

import com.oem.evpart.dto.request.InstalledPartRequest; // <--- KIỂM TRA TÊN NÀY
import com.oem.evpart.dto.response.InstalledPartResponse;
import java.util.List;

public interface InstalledPartService {
    // Chữ ký phương thức phải chính xác như này
    InstalledPartResponse recordPartInstallation(InstalledPartRequest request);

    List<InstalledPartResponse> getHistoryByVehicleVin(String vin);
    InstalledPartResponse updateInstallationStatus(Long installedId, String status);
}