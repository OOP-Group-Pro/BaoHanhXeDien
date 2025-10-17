package com.oem.evwarranty.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client cho Vehicle-Service
 */
@FeignClient(name = "vehicle-service") // Tên của Service trên Eureka/Service Registry
public interface VehicleServiceClient {

    // Xác thực VIN có tồn tại và thuộc về Customer hợp lệ không
    @GetMapping("/api/v1/vehicles/validate/{vin}")
    boolean validateVin(@PathVariable("vin") String vin);

    // Lấy tên khách hàng cho mục đích hiển thị trong ClaimDto
    @GetMapping("/api/v1/vehicles/{vin}/customer-name")
    String getCustomerNameByVin(@PathVariable("vin") String vin);
}
