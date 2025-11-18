package com.oem.evwarranty.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oem.evwarranty.dto.ApproveRequestDto;
import com.oem.evwarranty.dto.ClaimDto;
import com.oem.evwarranty.dto.CreateClaimDto;
import com.oem.evwarranty.dto.ClaimRepairResultDto;
import com.oem.evwarranty.model.AttachedDocument;
import com.oem.evwarranty.security.UserDetailsPrincipal;
import com.oem.evwarranty.service.FileStorageService;
import com.oem.evwarranty.service.WarrantyService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Sử dụng để validate DTO
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
public class WarrantyController {

    private final WarrantyService warrantyService;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;

    public WarrantyController(WarrantyService warrantyService, ObjectMapper objectMapper, FileStorageService fileStorageService) {
        this.warrantyService = warrantyService;
        this.objectMapper = objectMapper;
        this.fileStorageService = fileStorageService;
    }

    // --- A. NHÓM CHỨC NĂNG TẠO VÀ XEM ---

    /**
     * POST /api/v1/claims
     * Chức năng: Tạo yêu cầu bảo hành mới (Thực hiện bởi SC Staff)
     */
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }) // ⬅️ 1. Thêm 'consumes'
    public ResponseEntity<Long> createClaim(
            @RequestPart("dto") String dtoString, // ⬅️ 2. DTO giờ là String
            @RequestPart(value = "files", required = false) List<MultipartFile> files, // ⬅️ 3. Thêm file
            Authentication authentication
    ) {
        // Giả định chúng ta lấy ID nhân viên từ token JWT đã được xác thực
        UserDetailsPrincipal userPrincipal = (UserDetailsPrincipal) authentication.getPrincipal();
        Long scStaffId = userPrincipal.getUserId();
        try {
            // 5. Giải mã DTO từ String về Object
            CreateClaimDto createDto = objectMapper.readValue(dtoString, CreateClaimDto.class);

            // 6. Gọi Service (với signature mới)
            Long newClaimId = warrantyService.createClaim(createDto, files, scStaffId);
            return new ResponseEntity<>(newClaimId, HttpStatus.CREATED);

        } catch (JsonProcessingException e) {
            // Bắt lỗi nếu frontend gửi DTO string bị sai
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Bắt các lỗi nghiệp vụ khác
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/v1/claims/{claimId}
     * Chức năng: Xem chi tiết một yêu cầu bảo hành
     */
    @GetMapping("/{claimCode}")
    public ResponseEntity<ClaimDto> getClaimDetails(@PathVariable String claimCode) {
        ClaimDto claimDto = warrantyService.getClaimDetails(claimCode);
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
                                             @RequestBody ApproveRequestDto requestDto,
                                             Authentication authentication) {
        // Giả định lấy ID nhân viên EVM từ token
        UserDetailsPrincipal userPrincipal = (UserDetailsPrincipal) authentication.getPrincipal();
        Long evmStaffId = userPrincipal.getUserId();
        Long technicianId = requestDto.getTechnicianId();
        warrantyService.approveClaim(claimId, evmStaffId, requestDto.getApprovalNotes(), technicianId);
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
    @GetMapping("/{claimCode}/history")
    public ResponseEntity<?> getClaimStatusHistory(@PathVariable String claimCode) {
        // Thay vì trả về List<ClaimStatusLogDto>, bạn có thể trả về một đối tượng Response có List bên trong
        return ResponseEntity.ok(warrantyService.getClaimStatusHistory(claimCode));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SC_STAFF', 'EVM_STAFF', 'SC_TECHNICIAN')")
    @GetMapping
    public Page<ClaimDto> getClaims(
            @RequestParam(required = false) String claimCode,
            @RequestParam(required = false) String vin,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Pageable pageable,
            Authentication authentication
    ) {
        return warrantyService.getClaims(claimCode, vin, status, fromDate, toDate, pageable, authentication);
    }

    @GetMapping("/download-file/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        // 1. Tìm thông tin file trong DB (Bạn cần thêm hàm này vào Service/Repo)
        AttachedDocument doc = warrantyService.getDocumentById(id);

        // 2. Load file từ ổ cứng
        Resource file = fileStorageService.load(doc.getStoragePath());

        // 3. Trả về (dạng file tải xuống hoặc xem trực tiếp)
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, doc.getFileType())
                .body(file);
    }

}