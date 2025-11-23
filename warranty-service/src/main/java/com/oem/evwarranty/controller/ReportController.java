package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.ReportDataDto;
import com.oem.evwarranty.repository.ClaimPartDetailRepository;
import com.oem.evwarranty.repository.WarrantyClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final WarrantyClaimRepository claimRepo;
    private final ClaimPartDetailRepository partRepo;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Status
        try {
            List<Object[]> rawStatus = claimRepo.countClaimsByStatusRaw();
            List<ReportDataDto> statusStats = rawStatus.stream()
                    .map(row -> new ReportDataDto(
                            String.valueOf(row[0]),
                            ((Number) row[1]).longValue()
                    ))
                    .collect(Collectors.toList());
            stats.put("claimsByStatus", statusStats);
        } catch (Exception e) {
            log.error("❌ Lỗi Status Query", e);
            stats.put("claimsByStatus", new ArrayList<>());
        }

        // 2. Month
        try {
            int year = LocalDate.now().getYear();
            List<Object[]> rawMonth = claimRepo.countClaimsByMonthRaw(year);
            List<ReportDataDto> monthStats = rawMonth.stream()
                    .map(row -> new ReportDataDto(
                            String.valueOf(row[0]),
                            ((Number) row[1]).longValue()
                    ))
                    .collect(Collectors.toList());
            stats.put("claimsByMonth", monthStats);
        } catch (Exception e) {
            log.error("❌ Lỗi Month Query", e);
            stats.put("claimsByMonth", new ArrayList<>());
        }

        // 3. Parts
        try {
            stats.put("topFaultyParts", partRepo.findTopFaultyParts(PageRequest.of(0, 5)));
        } catch (Exception e) {
            log.error("❌ Lỗi Part Query", e);
            stats.put("topFaultyParts", new ArrayList<>());
        }

        return ResponseEntity.ok(stats);
    }

}