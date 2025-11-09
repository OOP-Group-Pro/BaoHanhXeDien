package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.ClaimDto;
import com.oem.evwarranty.service.ForVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ForVehicleServiceController {
    @Autowired
    private ForVehicleService forVehicleService;

    @GetMapping("/claims-by-status/{status}")
    public ResponseEntity<List<ClaimDto>> getClaimByStatus(@RequestParam @PathVariable("status") String status) {
        return ResponseEntity.ok().body(forVehicleService.getClaimsByStatus(status));
    }

}
