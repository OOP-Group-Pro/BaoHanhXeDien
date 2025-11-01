package com.oem.evwarranty.controller;

import com.oem.evwarranty.entity.NewStaffRequest;
import com.oem.evwarranty.service.NewStaffRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff-requests")
@RequiredArgsConstructor
public class NewStaffRequestController {

    private final NewStaffRequestService service;

    // 🟢 Manager gửi phiếu
    @PostMapping("/create")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> createRequest(@RequestBody NewStaffRequest request,
                                                Authentication authentication) {
        String createdBy = authentication.getName();
        service.createRequest(request, createdBy);
        return ResponseEntity.ok("Request submitted successfully");
    }

    // 🟢 Manager xem phiếu đã tạo
    @GetMapping("/my")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<NewStaffRequest>> getMyRequests(Authentication authentication) {
        String createdBy = authentication.getName();
        return ResponseEntity.ok(service.getRequestsByCreator(createdBy));
    }

    // 🟡 Admin xem tất cả phiếu
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NewStaffRequest>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }
    // 🟡 Admin chỉnh sửa phiếu trước khi duyệt
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NewStaffRequest> updateRequest(@PathVariable Long id,
                                                         @RequestBody NewStaffRequest updatedRequest) {
        NewStaffRequest request = service.updateRequest(id, updatedRequest);
        return ResponseEntity.ok(request);
    }

    // 🟡 Admin duyệt phiếu
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveRequest(@PathVariable Long id,
                                                 Authentication authentication) {
        System.out.println("🟢 ApproveRequest called, auth = " + authentication);
        String approvedBy = authentication.getName();
        service.approveRequest(id, approvedBy);
        return ResponseEntity.ok("Request approved and user created");
    }


    // 🟡 Admin từ chối phiếu
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id,
                                                Authentication authentication) {
        String approvedBy = authentication.getName();
        service.rejectRequest(id, approvedBy);
        return ResponseEntity.ok("Request rejected");
    }
}
