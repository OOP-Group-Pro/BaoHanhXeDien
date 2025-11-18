package com.oem.evwarranty.client.warranty.fallback;

import com.oem.evwarranty.client.warranty.VehicleServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// VehicleServiceClientFallback.java
@Slf4j
@Component
public class VehicleServiceClientFallback implements VehicleServiceClient {

    @Override
    public boolean validateVin(String vin) {
        // Luôn trả về FALSE an toàn khi Vehicle Service DOWN
        log.error("Feign client ERROR: Cannot validate vin");
        return false;
    }

    @Override
    public String getCustomerNameByVin(String vin) {
        // Trả về chuỗi an toàn để tránh lỗi hiển thị
        log.error("Feign client ERROR: Cannot get customer name by vin");
        return "Lỗi kết nối/Thông tin xe không khả dụng";
    }

    @Override
    public Map<String, String> getCustomerNamesByVins (List<String> vin) {
        log.error("Feign client ERROR: Cannot get customer names by vin");
        return null;
    }
}
