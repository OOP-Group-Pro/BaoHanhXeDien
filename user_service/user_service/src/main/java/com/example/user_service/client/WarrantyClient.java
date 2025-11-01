package com.example.user_service.client;

import com.example.user_service.dto.request.WarrantyRequest;
import com.example.user_service.dto.response.WarrantyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import com.example.user_service.client.fallback.WarrantyClientFallback;

@FeignClient(
        name = "warranty-service",
        url = "${service.warranty.url}",
        fallback = WarrantyClientFallback.class
)
public interface WarrantyClient {

    @PostMapping("/api/v1/warranties")
    ResponseEntity<Void> createWarranty(@RequestBody WarrantyRequest request);

    @GetMapping("/api/v1/warranties/{id}")
    WarrantyResponse getWarrantyById(@PathVariable Long id);
}
