package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.*;
import com.oem.evwarranty.mapper.DocumentMapper;
import com.oem.evwarranty.mapper.PartMapper;
import com.oem.evwarranty.model.AttachedDocument;
import com.oem.evwarranty.model.utils.UserResponseDto;
import com.oem.evwarranty.client.warranty.UserServiceClient;
import com.oem.evwarranty.enums.ClaimStatus;
import com.oem.evwarranty.mapper.ClaimMapper;
import com.oem.evwarranty.model.ClaimPartDetail;
import com.oem.evwarranty.model.ClaimStatusLog;
import com.oem.evwarranty.model.WarrantyClaim;
import com.oem.evwarranty.model.utils.PartAllocationRequest;
import com.oem.evwarranty.model.utils.SerialUpdateDetail;
import com.oem.evwarranty.client.warranty.VehicleServiceClient; // Feign Client
import com.oem.evwarranty.client.warranty.PartServiceClient;    // Feign Client
import com.oem.evwarranty.repository.AttachedDocumentRepository;
import com.oem.evwarranty.repository.specification.WarrantyClaimSpecification;
import com.oem.evwarranty.security.UserDetailsPrincipal;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


import com.oem.evwarranty.repository.ClaimPartDetailRepository;
import com.oem.evwarranty.repository.ClaimStatusLogRepository;
import com.oem.evwarranty.repository.WarrantyClaimRepository;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class WarrantyService {

    // 1. Dependencies Nội bộ (Repositories)
    private final WarrantyClaimRepository claimRepo;
    private final ClaimStatusLogRepository logRepo;
    private final ClaimPartDetailRepository partDetailRepo;
    private final AttachedDocumentRepository attachedDocRepo;
    // ... (Các Repository khác: ClaimCostRepository, AttachedDocumentRepository)

    // 2. Dependencies Liên Service (Feign Clients)
    private final VehicleServiceClient vehicleClient;
    private final PartServiceClient partClient;
    private final UserServiceClient userClient;

    private final FileStorageService fileStorageService;

    // Constructor Injection (Spring Boot tự động tiêm các dependency này)
    public WarrantyService(
            WarrantyClaimRepository claimRepo,
            ClaimStatusLogRepository logRepo,
            ClaimPartDetailRepository partDetailRepo,
            AttachedDocumentRepository attachedDocRepo,
            VehicleServiceClient vehicleClient,
            PartServiceClient partClient,
            UserServiceClient userClient,
            FileStorageService fileStorageService) {

        this.claimRepo = claimRepo;
        this.logRepo = logRepo;
        this.partDetailRepo = partDetailRepo;
        this.attachedDocRepo = attachedDocRepo;
        this.vehicleClient = vehicleClient;
        this.partClient = partClient;
        this.userClient = userClient;
        this.fileStorageService = fileStorageService;
    }

    // --- BẮT ĐẦU CÁC PHƯƠNG THỨC NGHIỆP VỤ ---

    // ⬇️ BƯỚC 3: HÀM CREATECLAIM (ĐÃ SỬA HOÀN CHỈNH) ⬇️
    @Transactional
    @CacheEvict(value = "claim_list", allEntries = true)
    public Long createClaim(CreateClaimDto dto, List<MultipartFile> files, Long scStaffId) {

        // BƯỚC 1: XÁC THỰC
        boolean isVinValid = vehicleClient.validateVin(dto.getVin());
        if (!isVinValid) {
            throw new IllegalArgumentException("VIN không hợp lệ hoặc không tồn tại.");
        }
        UserResponseDto userInfo = userClient.getScStaffById(scStaffId);

        // BƯỚC 2.1: TẠO MÃ CLAIM
        String claimCode = generateClaimCode();



        // BƯỚC 3: KHỞI TẠO ENTITY (SỬA LỖI BUILDER Ở ĐÂY)
        WarrantyClaim newClaim = WarrantyClaim.builder()
                .claimCode(claimCode)
                .vin(dto.getVin())
                .scStaffId(scStaffId)
                .centerId(userInfo.getServiceCenterId())
                .description(dto.getDescription())
                .currentStatus(ClaimStatus.WAITING_APPROVAL)
                .dateCreated(LocalDateTime.now())
                .technicalStaffId(null)
                // ❌ ĐÃ XÓA DÒNG: .documents(files) -> Vì sai kiểu dữ liệu
                .build();

        /*
        Dat log kiem tra co nhan duoc part request dto khong:
         */
        List<ClaimPartDetail> partDetails = new ArrayList<>();
        if (dto.getRequestedParts().isEmpty() || dto.getRequestedParts() == null) {
            log.error("😡😡😡 Have not get requested parts.");
        } else {
            // BUOC 2.2: Tao danh sach part can sua chua:
            partDetails = dto.getRequestedParts().stream()
                    .map(partDto -> {
                        ClaimPartDetail part = PartMapper.mapRequestToClaimPartDetail(partDto);
                        part.setClaim(newClaim);
                        return part;
                    })
                    .peek(part -> {
                        log.info("👀👀👀Found part: {}", part.getPartName());
                    })
                    .toList();
        }
        // Luu danh sach part can sua chua vao claim moi:
        newClaim.setPartDetails(partDetails);

        if (newClaim.getPartDetails().isEmpty() || newClaim.getPartDetails() == null) {
            log.error("😭😭😭 Have not get requested parts.");
        }

        // Khởi tạo danh sách documents rỗng để tránh NullPointerException
        if (newClaim.getDocuments() == null) {
            newClaim.setDocuments(new ArrayList<>());
        }

        // BƯỚC 4: XỬ LÝ FILE (SỬA LẠI ĐỂ DÙNG SERVICE THẬT)
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                // Gọi Service thật để lưu vào ổ cứng
                // Hàm này sẽ trả về đường dẫn dạng: "claims/CLAIM-001/anh.jpg"
                String relativePath = fileStorageService.save(file, "claims/" + claimCode);

                // Tạo Entity AttachedDocument
                AttachedDocument doc = new AttachedDocument();
                doc.setFileName(file.getOriginalFilename());
                doc.setFileType(file.getContentType());
                doc.setStoragePath(relativePath);
                doc.setUploadDate(LocalDateTime.now());
                doc.setClaim(newClaim); // Liên kết ngược lại Claim

                // Thêm vào danh sách (sẽ được lưu tự động nhờ Cascade)
                newClaim.getDocuments().add(doc);
            }
        }

        // BƯỚC 5: LƯU CLAIM
        // JPA sẽ lưu newClaim -> sau đó tự động lưu danh sách documents bên trong
        claimRepo.save(newClaim);
        newClaim.getPartDetails().forEach(part -> {
            log.info("🥹🥹🥹 Look up claim part: ", part.getPartName());
        });

        // 6b. Lưu Log
        logRepo.save(
                ClaimStatusLog.builder()
                        .claim(newClaim)
                        .timestamp(LocalDateTime.now())
                        .status(ClaimStatus.WAITING_APPROVAL)
                        .processorId(scStaffId)
                        .notes("Yêu cầu bảo hành được khởi tạo.")
                        .build());

        return newClaim.getId();
    }


    // 2. Chức năng: PHÊ DUYỆT CLAIM (EVM STAFF)
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "claim_details", key = "#claimCode"), // Xóa cache chi tiết của claim này (nếu key là claimCode, cần sửa lại hàm getClaimDetails để dùng claimCode làm key, hoặc dùng claimId nếu sửa controller)
            // LƯU Ý: Ở đây hàm approve nhận claimId (Long), nhưng hàm getClaimDetails lại dùng claimCode (String).
            // Để cache hoạt động hiệu quả, nên thống nhất dùng 1 loại key hoặc phải query DB để lấy code từ ID trước khi evict.
            // Cách đơn giản nhất: Xóa toàn bộ cache chi tiết (hơi tốn kém nhưng an toàn) hoặc chấp nhận cache cũ trong 10p.
            // Ở đây tôi dùng allEntries = true cho an toàn nhất trong giai đoạn này.
            @CacheEvict(value = "claim_details", allEntries = true),
            @CacheEvict(value = "claim_history", allEntries = true),
            @CacheEvict(value = "claim_list", allEntries = true)
    })
    public void approveClaim(String claimCode, Long evmStaffId, String approvalNotes, Long technicalStaffId) {
        // [Logic chính]:
        // 1. Kiểm tra trạng thái hiện tại (phải là WAITING_APPROVAL).
        // 2. Cập nhật trạng thái Claim.
        // 3. Cập nhật trạng thái phê duyệt cho ClaimPartDetail.
        // 4. Ghi Log.
        // 5. Yêu cầu cấp phát phụ tùng (Gọi Part-Service).
        /*  ### Chu y: O day,   */
        WarrantyClaim claim = claimRepo.findByClaimCode(claimCode)
                .orElseThrow(() -> new IllegalArgumentException("Claim không tồn tại."));

        if (claim.getCurrentStatus() != ClaimStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("Claim không đủ điều kiện phê duyệt. Trạng thái hiện tại: " + claim.getCurrentStatus());
        }

        // BƯỚC 1 & 2: CẬP NHẬT CLAIM
        claim.setCurrentStatus(ClaimStatus.APPROVED);   // Cong viec thuc hien qpprove nằm o đây
        claim.setTechnicalStaffId(technicalStaffId);  // Lưu Chuyên viên sửa chữa
        claimRepo.save(claim);

        // BƯỚC 3: CẬP NHẬT CHI TIẾT PHỤ TÙNG
        // Giả sử logic phê duyệt là phê duyệt tất cả các part trong claim này
        // Thuc ra de cho dung thi phai kiem tra moi part co trong chinh sach bao hanh hay khong, trong kho con hay khong -> vay thi phai truy cuu den Doi tuong part -> vay thi phai goi api den Part-service de lay thong tin hoac thuc hien method kiem tra luon
        List<ClaimPartDetail> parts = partDetailRepo.findAllByClaim_Id(claim.getId());
        List<String> partNumbers = new ArrayList<>();   // Danh sách phụ tùng đã được duyệt
        for (ClaimPartDetail part : parts) {
            // Nếu có kiểm tra các điều kiện phê duyệt phụ tùng thậm chí là gọi đến part-service để kiểm tra thì sẽ thực hiện ở đây. Nhưng hiện tại cho đơn giản thì duyệt hết mà ko cần ktra
            // Thường thì viec kiểm tra và xem xét từng phụ tùng có được duyệt bảo hành hay không sẽ thực hiện ở đây rồi mới đánh dấu và thêm vào partsNumbers
            part.setIsApproved(true);   // Đánh dấu phụ tùng được phép bảo hành
            partNumbers.add(part.getPartNumber());  // Sau khi đánh dấu thì thêm vào danh sách đã duyệt
        }
        partDetailRepo.saveAll(parts);  //Lưu cập nhật trạng thái duyệt mới của tất cả part

        // BƯỚC 4: GHI LOG
        logRepo.save(
                ClaimStatusLog.builder()
                        .claim(claim)
                        .timestamp(LocalDateTime.now())
                        .status(ClaimStatus.APPROVED)
                        .processorId(evmStaffId)
                        .notes(approvalNotes != null ? approvalNotes : "Yêu cầu đã được EVM phê duyệt.")
                        .build());

        // BƯỚC 5: GỌI SERVICE NGOÀI
        try {
            List<ClaimPartDetail> partRequests = claim.getPartDetails();

            if (partRequests != null && !partRequests.isEmpty()) {

                // 1. Map từ ClaimPartDetail sang PartRequestItem
                List<PartAllocationRequest.PartRequestItem> items = partRequests.stream()
                        .map(p -> PartAllocationRequest.PartRequestItem.builder()
                                .partNumber(p.getPartNumber())      // Mã SKU (Type)
                                .quantity(p.getQuantityRequired())  // Số lượng thực tế cần
                                .build())
                        .toList();

                // 2. Tạo Request
                PartAllocationRequest allocationRequest = PartAllocationRequest.builder()
                        .claimCode(claim.getClaimCode())
                        .serviceCenterId(claim.getCenterId())
                        .items(items) // ⬅️ Gửi danh sách chi tiết
                        .build();

                // 3. Gửi đi
                partClient.requestPartAllocation(allocationRequest);
            }
        } catch (Exception e) {
            log.error("❌ Lỗi cấp phát phụ tùng: {}", e.getMessage());
            // Tùy chọn: throw e nếu muốn rollback transaction khi lỗi
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "claim_details", allEntries = true),
            @CacheEvict(value = "claim_history", allEntries = true),
            @CacheEvict(value = "claim_list", allEntries = true)
    })
    public void rejectClaim(String claimCode, Long evmStaffId, String reason) {
        // B1: Xac dinh claim bi tu choi
        // B2: Kiem tra co reject claim duoc hay khong -> kiem tra trang thai claim -> Chi co the reject neu claim dang trong status WAITING_APPROVAL
        // B3: Reject -> cap nhat currentStatus = ClaimStatus.REJECTED -> Luu lai: claimRepo.save(claim)
        // B4: Ghi vao log
        WarrantyClaim claim = claimRepo.findByClaimCode(claimCode).orElseThrow(() -> new IllegalArgumentException("Claim không tồn tại"));

        if(claim.getCurrentStatus() != ClaimStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("Claim không thể bị từ chối ");
        }

        claim.setCurrentStatus(ClaimStatus.REJECTED);
        claim.setTechnicalStaffId(evmStaffId);  // Lưu Chuyên viên sửa chữa
        claimRepo.save(claim);
        logRepo.save(ClaimStatusLog.builder()
                .claim(claim)
                .timestamp(LocalDateTime.now())
                .status(ClaimStatus.REJECTED)
                .processorId(evmStaffId)
                .notes(reason != null ? reason : "Yêu cầu đã bị từ chối.")
                .build()
        );
    }

    // Bước dưới đây giống như bước kiểm tra và đánh giá hiệu quả của công tác bảo hành ấy
    // 3. Chức năng: CẬP NHẬT KẾT QUẢ SỬA CHỮA (Lắp/Tháo Serial Number)
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "claim_details", allEntries = true),
            @CacheEvict(value = "claim_history", allEntries = true),
            @CacheEvict(value = "claim_list", allEntries = true)
    })
    public void updateRepairResult(Long claimId, ClaimRepairResultDto resultDto, Long currentTechnicianId) {

        WarrantyClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim không tồn tại."));

        // Kiểm tra trạng thái phù hợp để cập nhật kết quả
        if (claim.getCurrentStatus() != ClaimStatus.APPROVED) {
            throw new IllegalArgumentException("Claim chưa được duyệt để cập nhật kết quả.");
        }

        Boolean allPartsUpdated = false;

        // Lặp qua danh sách kết quả (từ DTO)
        for (Map.Entry<String, SerialUpdateDetail> entry : resultDto.getSerialUpdates().entrySet()) {
            String partNumber = entry.getKey();
            SerialUpdateDetail newSerialNumber = entry.getValue();
            // Lấy ClaimPartDetail tương ứng (bằng claimId và partNumber)
            ClaimPartDetail partDetail = partDetailRepo.findByClaim_IdAndPartNumber(claimId, partNumber);
            // Cập nhật partDetail.setSerialNumberReplace(newSerialNumber);
            partDetail.setSerialNumberReplace(newSerialNumber.getNewSerialNumber());
            // Cập nhật partDetail.setSerialNumberDefective(oldSerialNumber);
            partDetail.setSerialNumberDefective(newSerialNumber.getDefectiveSerialNumber());
            // Lưu lại:
            partDetailRepo.save(partDetail);
        }

        // Logic kiểm tra nếu mọi thứ đã xong -> Cập nhật Claim Status thành COMPLETED
        claim.setCurrentStatus(ClaimStatus.COMPLETED);
        claimRepo.save(claim);
        // ... (Ghi Log)
        logRepo.save(ClaimStatusLog.builder()
                .claim(claim)
                .timestamp(LocalDateTime.now())
                .status(ClaimStatus.COMPLETED)
                .processorId(claim.getTechnicalStaffId())
                .notes("Công tác bảo hành đã hoàn thành.")
                .build());
    }

    // --- CÁC HÀM CƠ BẢN (READ/GET) ---

    // 4. Chức năng: XEM CHI TIẾT CLAIM (BẢN NÂNG CẤP CUỐI CÙNG)
    @Transactional(readOnly = true)
    // 🚀 CACHE: Cache chi tiết Claim.
    // Key là claimCode.
    @Cacheable(value = "claim_details", key = "#claimCode")
    public ClaimDto getClaimDetails(String claimCode) {
        System.out.println(">>> Getting Claim Details from DB for Code: " + claimCode);
        WarrantyClaim claim = claimRepo.findByClaimCode(claimCode)
                .orElseThrow(() -> new IllegalArgumentException("Claim không tồn tại."));
        claim.getPartDetails().forEach( part -> {
                    log.info("⁉️⁉️ Check partDetail of claim {} in getClaimDetails func with it's partDetails: {}", claim.getClaimCode(), part.getPartName());
                });

        // 1. Chuyển đổi cơ bản (dùng Mapper)
        ClaimDto claimDto = ClaimMapper.mapToClaimDto(claim);
        claimDto.getPartList().forEach( part -> {
            log.info("⁉️⁉️ Check partDetail of claimDto of {} in getClaimDetails func with it's partDetails: {}", claim.getClaimCode(), part.getPartName());
        });

        // 2. Lấy Tên Khách hàng
        try {
            String customerName = vehicleClient.getCustomerNameByVin(claim.getVin());
            claimDto.setCustomerName(customerName);
        } catch (Exception e) {
            claimDto.setCustomerName("(Không tìm thấy xe)");
        }

        // 3. Lấy Tên Kỹ thuật viên
        if (claim.getTechnicalStaffId() != null) {
            try {
                UserResponseDto tech = userClient.getScStaffById(claim.getTechnicalStaffId());
                claimDto.setTechnicalName(tech.getFullName());
                // ⬅️ Đã xóa dòng setTechnicalId (Sửa lỗi 1)
            } catch (Exception e) {
                claimDto.setTechnicalName("(Lỗi: Không tìm thấy KTV)");
            }
        } else {
            claimDto.setTechnicalName("(Chưa gán)");
        }

        // 4. ⬇️ NÂNG CẤP: Lấy Tên Phụ tùng (Sửa lỗi 2) ⬇️

        // 4a. Gom các partNumber từ claim
        List<String> partNumbers = claim.getPartDetails().stream()
                .map(ClaimPartDetail::getPartNumber)
                .distinct()
                .toList();



        List<ClaimPartDetailDto> partDtos = claim.getPartDetails().stream()
                .map(entity -> {
                    // Dùng PartMapper cũ
                    ClaimPartDetailDto dto = PartMapper.mapToClaimPartDetailDto(entity);
                    log.info("⁉️⁉️⁉️⁉️Test are there parts already had in claim: {}", entity.getPartName());

                    dto.setPartName(entity.getPartName()); // Giả sử hàm là .getName()

                    return dto;
                })
                .collect(Collectors.toList());

        claimDto.setPartList(partDtos);

        // ⬇️ 5. (BỔ SUNG) MAP DANH SÁCH TÀI LIỆU (Fix lỗi "Không có tài liệu")
        if (claim.getDocuments() != null && !claim.getDocuments().isEmpty()) {
            List<AttachedDocumentDto> docDtos = claim.getDocuments().stream()
                    .map(DocumentMapper::mapToAttachedDocumentDto) // ⬅️ Đã dùng Class 'DocumentMapper'
                    .toList(); // Hoặc .collect(Collectors.toList()) nếu Java cũ
            claimDto.setDocuments(docDtos);
        } else {
            claimDto.setDocuments(new ArrayList<>()); // Trả về list rỗng thay vì null
        }

        return claimDto;
    }

    // 5. Chức năng: XEM LỊCH SỬ TRẠNG THÁI
    @Transactional(readOnly = true)
    @Cacheable(value = "claim_history", key = "#claimCode")
    public List<ClaimStatusLogDto> getClaimStatusHistory(String claimCode) {
        // Sử dụng phương thức findByClaimId trong LogRepo
        List<ClaimStatusLog> logs = logRepo.findByClaim_ClaimCodeOrderByTimestampAsc(claimCode);

        // map sang DTO trước khi trả về
        return logs.stream()
                // Chuyển từng ClaimStatusLog thành Dto
                .map(ClaimMapper::mapToLogDto)
                // Dùng user client gửi ProcessorId để lấy tên Processor và lưu vào Dto
                .map(logDto -> {
                    if(logDto.getProcessorId() != null) {
                        try {
                            UserResponseDto proccessorDetails = userClient.getScStaffById(logDto.getProcessorId());
                            logDto.setProcessorName(proccessorDetails.getFullName());
                        }catch (Exception e) {
                            logDto.setProcessorName("ID: " + logDto.getProcessorId() + " (Không tìm thấy)");
                        }
                    }else logDto.setProcessorName("Hệ thống");

                    return logDto;
                })
                .toList();
    }

    // --- PRIVATE UTILS ---
    private String generateClaimCode() {
        // Logic tạo mã Claim duy nhất (Ví dụ: WC-2025-00001)
        return "WC-" + LocalDateTime.now().getYear() + "-" + System.currentTimeMillis() % 100000;
    }

    // =======Ham lay claims duoc loc:========
    @Cacheable(
            value = "claim_list",
            key = "(#claimCode ?: '') + '-' + " +
                    "(#vin ?: '') + '-' + " +
                    "(#statusStr ?: '') + '-' + " +
                    "(#fromDate ?: '') + '-' + " +
                    "(#toDate ?: '') + '-' + " +
                    "#pageable.pageNumber + '-' + #pageable.pageSize"
    )
    public PageCacheDto<ClaimDto> getClaims(
            String claimCode,
            String vin,
            String statusStr,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable,
            Authentication authentication
    ) {
        Specification<WarrantyClaim> specification = Specification.where(null);

        // Convert String to Enum:
        ClaimStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = ClaimStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value: " + statusStr);
            }
        }

        specification = specification.and(WarrantyClaimSpecification.hasClaimCode(claimCode))
                .and(WarrantyClaimSpecification.hasVin(vin));

        if (status != null) {
            specification = specification.and(WarrantyClaimSpecification.hasStatus(status));
        }

        specification = specification.and(WarrantyClaimSpecification.createdAfter(fromDate))
                .and(WarrantyClaimSpecification.createdBefore(toDate));

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsPrincipal userDetails) {

            Long currentUserId = userDetails.getUserId();
            Long currentCenterId = userDetails.getCenterId();

            List<String> roles = authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .toList();

            if (!roles.contains("ROLE_ADMIN") &&
                    !roles.contains("ROLE_EVM_STAFF")) {

                if (roles.contains("ROLE_MANAGER")) {
                    specification = specification.and(WarrantyClaimSpecification.hasCenterId(currentCenterId));
                }if (roles.contains("ROLE_SC_TECHNICIAN")) {
                    Specification<WarrantyClaim> assignedToMe = (root, query, cb) ->
                        cb.equal(root.get("technicalStaffId"), currentUserId);
                    specification = specification.and(assignedToMe);}
                else {
                    specification = specification.and(WarrantyClaimSpecification.hasStaffId(currentUserId));
                }
            }

        } else if (!(principal instanceof String && principal.equals("internal-service"))) {
            throw new AccessDeniedException("Cannot determine user identity");
        }

        Page<WarrantyClaim> claimPage = claimRepo.findAll(specification, pageable);

        List<Long> technicianIds = claimPage.getContent().stream()
                .map(WarrantyClaim::getTechnicalStaffId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        List<String> vins = claimPage.getContent().stream()
                .map(WarrantyClaim::getVin)
                .distinct()
                .toList();

        Map<String, String> customerNameMap = Optional.ofNullable(
                vehicleClient.getCustomerNamesByVins(vins)
        ).orElse(Collections.emptyMap());

        Map<Long, UserResponseDto> technicianMap = Optional.ofNullable(
                userClient.getUserDetailsMap(technicianIds)
        ).orElse(Collections.emptyMap());

        List<ClaimDto> claimDtos = claimPage.getContent().stream()
                .filter(Objects::nonNull) // loại bỏ item null
                .map(claim -> {
                    ClaimDto dto = ClaimMapper.mapToClaimDto(claim);
                    dto.setCustomerName(customerNameMap.getOrDefault(dto.getVin(), "(Không tìm thấy)"));
                    if (claim.getTechnicalStaffId() != null) {
                        UserResponseDto userInfo = technicianMap.get(claim.getTechnicalStaffId());
                        if (userInfo != null) {
                            dto.setTechnicalName(userInfo.getFullName());
                        }
                    }
                    return dto;
                })
                .toList();


        return PageCacheDto.from(
                new PageImpl<>(
                        Optional.ofNullable(claimDtos).orElse(Collections.emptyList()),
                        pageable,
                        claimPage.getTotalElements()
                )
        );

    }


    public AttachedDocument getDocumentById(Long id) {
        return attachedDocRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Cannot find document with id: " + id));
    }
}