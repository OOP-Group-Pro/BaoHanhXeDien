package com.oem.evwarranty.controller;


import com.oem.evwarranty.dto.ClaimDto;
import com.oem.evwarranty.dto.CreateClaimDto;
import com.oem.evwarranty.dto.ClaimRepairResultDto;
import com.oem.evwarranty.service.WarrantyService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Sử dụng để validate DTO

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:63342")
@RequestMapping("/api/v1/claims")
public class WarrantyController {

    private final WarrantyService warrantyService;

    public WarrantyController(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    // --- A. NHÓM CHỨC NĂNG TẠO VÀ XEM ---

    /**
     * POST /api/v1/claims
     * Chức năng: Tạo yêu cầu bảo hành mới (Thực hiện bởi SC Staff)
     */
    @PostMapping
    // @PreAuthorize("hasRole('SC_STAFF')") // Ví dụ về phân quyền
    public ResponseEntity<Long> createClaim(@Valid @RequestBody CreateClaimDto createDto) {
        // Giả định chúng ta lấy ID nhân viên từ token JWT đã được xác thực
        Long scStaffId = 101L;
        Long newClaimId = warrantyService.createClaim(createDto, scStaffId);
        return new ResponseEntity<>(newClaimId, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/claims/{claimId}
     * Chức năng: Xem chi tiết một yêu cầu bảo hành
     */
    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimDto> getClaimDetails(@PathVariable Long claimId) {
        ClaimDto claimDto = warrantyService.getClaimDetails(claimId);
        return ResponseEntity.ok(claimDto);
    }

    // --- B. NHÓM CHỨC NĂNG CẬP NHẬT TRẠNG THÁI/PHÊ DUYỆT ---

    /**
     * PUT /api/v1/claims/{claimId}/approve
     * Chức năng: Phê duyệt yêu cầu bảo hành (Thực hiện bởi EVM Staff)
     */
    @PutMapping("/{claimId}/approve")
    // @PreAuthorize("hasRole('EVM_STAFF')")
    public ResponseEntity<Void> approveClaim(@PathVariable Long claimId,
                                             @RequestParam(required = false) String approvalNotes) {
        // Giả định lấy ID nhân viên EVM từ token
        Long evmStaffId = 202L;
        warrantyService.approveClaim(claimId, evmStaffId, approvalNotes);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/v1/claims/{claimId}/reject
     * Chức năng: Từ chối yêu cầu bảo hành (Thực hiện bởi EVM Staff)
     */
    @PutMapping("/{claimId}/reject")
    // @PreAuthorize("hasRole('EVM_STAFF')")
    public ResponseEntity<Void> rejectClaim(@PathVariable Long claimId,
                                            @RequestParam String reason) { // Lý do từ chối là bắt buộc
        Long evmStaffId = 202L;
        warrantyService.rejectClaim(claimId, evmStaffId, reason); // Cần thêm rejectClaim() vào Service
        return ResponseEntity.ok().build();
    }

    // --- C. NHÓM CHỨC NĂNG CẬP NHẬT KẾT QUẢ/PHỤ TRỢ ---

    /**
     * PUT /api/v1/claims/{claimId}/repair-result
     * Chức năng: Cập nhật kết quả sửa chữa (số serial mới/cũ) (Thực hiện bởi Technical Staff)
     */
    @PutMapping("/{claimId}/repair-result")
    // @PreAuthorize("hasRole('TECHNICAL_STAFF')")
    public ResponseEntity<Void> updateRepairResult(@PathVariable Long claimId,
                                                   @Valid @RequestBody ClaimRepairResultDto resultDto) {
        // Logic service sẽ tự kiểm tra trạng thái Claim
        warrantyService.updateRepairResult(claimId, resultDto);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/v1/claims/{claimId}/history
     * Chức năng: Xem lịch sử trạng thái của Claim
     */
    @GetMapping("/{claimId}/history")
    public ResponseEntity<?> getClaimStatusHistory(@PathVariable Long claimId) {
        // Thay vì trả về List<ClaimStatusLogDto>, bạn có thể trả về một đối tượng Response có List bên trong
        return ResponseEntity.ok(warrantyService.getClaimStatusHistory(claimId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<ClaimDto>> getAllClaims() {
        return ResponseEntity.ok(warrantyService.getAllClaims());
    }


    @GetMapping("/claims-by-status/{status}")
    public ResponseEntity<List<ClaimDto>> getClaimsByStatus(@RequestParam @PathVariable("status") String status) {
        return ResponseEntity.ok().body(warrantyService.getClaimsByStatus(status));
    }
}
