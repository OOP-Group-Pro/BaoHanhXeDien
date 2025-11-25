package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.*;
import com.oem.evwarranty.mapper.DocumentMapper;
import com.oem.evwarranty.mapper.PartMapper;
import com.oem.evwarranty.model.AttachedDocument;
import com.oem.evwarranty.model.utils.CompleteAllocationRequest;
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
    //Firebase nofication
    private final FCMService fcmService;

    private final FileStorageService fileStorageService;

    // Constructor Injection (Spring Boot tự động tiêm các dependency này)
    public WarrantyService(
            WarrantyClaimRepository claimRepo,
            ClaimStatusLogRepository logRepo,
            ClaimPartDetailRepository partDetailRepo,
            AttachedDocumentRepository attachedDocRepo,
            VehicleServiceClient vehicleClient,
            PartServiceClient partClient,
            UserServiceClient userClient, FCMService fcmService,
            FileStorageService fileStorageService) {

        this.claimRepo = claimRepo;
        this.logRepo = logRepo;
        this.partDetailRepo = partDetailRepo;
        this.attachedDocRepo = attachedDocRepo;
        this.vehicleClient = vehicleClient;
        this.partClient = partClient;
        this.userClient = userClient;
        this.fcmService = fcmService;
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
        log.info("🔵 DEBUG: approveClaim called with ClaimCode={}, TechID={}", claimCode, technicalStaffId);

        // 1. Tìm và Validate Claim
        WarrantyClaim claim = claimRepo.findByClaimCode(claimCode)
                .orElseThrow(() -> new IllegalArgumentException("Claim không tồn tại."));

        if (claim.getCurrentStatus() != ClaimStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("Claim không đủ điều kiện phê duyệt. Trạng thái hiện tại: " + claim.getCurrentStatus());
        }

        // 2. [QUAN TRỌNG] GỌI PART-SERVICE ĐỂ CẤP PHÁT (Thực hiện trước khi update DB)
        try {
            List<ClaimPartDetail> partRequests = claim.getPartDetails();

            if (partRequests != null && !partRequests.isEmpty()) {
                // Map request
                List<PartAllocationRequest.PartRequestItem> items = partRequests.stream()
                        .map(p -> PartAllocationRequest.PartRequestItem.builder()
                                .partNumber(p.getPartNumber())      // SKU/Type
                                .quantity(p.getQuantityRequired())
                                .build())
                        .toList();

                PartAllocationRequest allocationRequest = PartAllocationRequest.builder()
                        .claimCode(claim.getClaimCode())
                        .serviceCenterId(claim.getCenterId())
                        .items(items)
                        .build();

                // Gọi Feign Client
                partClient.requestPartAllocation(allocationRequest);
            }
        } catch (Exception e) {
            log.error("❌ Lỗi cấp phát phụ tùng: {}", e.getMessage());
            // BẮT BUỘC THROW ĐỂ ROLLBACK TRANSACTION
            throw new RuntimeException("Không thể duyệt vì lỗi kho: " + e.getMessage());
        }

        // 3. CẬP NHẬT CLAIM & PART DETAILS
        claim.setCurrentStatus(ClaimStatus.APPROVED);
        claim.setEvmStaffId(evmStaffId);
        claim.setTechnicalStaffId(technicalStaffId); // Gán KTV

        List<ClaimPartDetail> parts = partDetailRepo.findAllByClaim_Id(claim.getId());
        for (ClaimPartDetail part : parts) {
            part.setIsApproved(true); // Đánh dấu đã duyệt
        }

        // Lưu xuống DB (Do Transaction nên sẽ commit cùng lúc khi hết hàm)
        claimRepo.save(claim);
        // partDetailRepo.saveAll(parts); // Không cần thiết vì claim.getPartDetails() là managed entity, nhưng giữ cũng không sao.

        // 4. GHI LOG LỊCH SỬ
        logRepo.save(
                ClaimStatusLog.builder()
                        .claim(claim)
                        .timestamp(LocalDateTime.now())
                        .status(ClaimStatus.APPROVED)
                        .processorId(evmStaffId)
                        .notes(approvalNotes != null ? approvalNotes : "Yêu cầu đã được EVM phê duyệt.")
                        .build());

        // 5. [TÍNH NĂNG MỚI] GỬI THÔNG BÁO FIREBASE (FIRE & FORGET)
        // Đặt cuối cùng để không ảnh hưởng luồng chính nếu lỗi mạng
        try {
            if (technicalStaffId != null) {
                // Gọi User Service lấy thông tin KTV (để lấy Token FCM)
                UserResponseDto technician = userClient.getScStaffById(technicalStaffId);

                log.info("🔍 CHECK TECHNICIAN TOKEN: {}", technician != null ? technician.getFcmToken() : "NULL");

                if (technician != null && technician.getFcmToken() != null && !technician.getFcmToken().isEmpty()) {
                    fcmService.sendNotification(
                            technician.getFcmToken(),
                            "Nhiệm vụ mới! 🛠️",
                            "Bạn được phân công xử lý phiếu: " + claimCode
                    );
                    log.info("🔔 Đã gửi thông báo FCM cho Technician: {}", technician.getFullName());
                } else {
                    log.warn("⚠️ Technician chưa có FCM Token, bỏ qua gửi thông báo.");
                }
            }
        } catch (Exception e) {
            // Chỉ log warning, KHÔNG throw exception để tránh rollback việc duyệt Claim đã thành công
            log.warn("⚠️ Lỗi gửi thông báo FCM (nhưng Claim vẫn được duyệt): {}", e.getMessage());
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

        if (claim.getCurrentStatus() != ClaimStatus.APPROVED) {
            throw new IllegalArgumentException("Claim chưa được duyệt để cập nhật kết quả.");
        }

        // --- BƯỚC 1: CẬP NHẬT SERIAL (VÒNG LẶP) ---
        for (Map.Entry<String, SerialUpdateDetail> entry : resultDto.getSerialUpdates().entrySet()) {
            String partNumber = entry.getKey();
            SerialUpdateDetail newSerialNumber = entry.getValue();

            // Lấy ClaimPartDetail
            ClaimPartDetail partDetail = partDetailRepo.findByClaim_IdAndPartNumber(claimId, partNumber);

            if (partDetail != null) {
                // Cập nhật Serial Mới & Cũ
                partDetail.setSerialNumberReplace(newSerialNumber.getNewSerialNumber());
                partDetail.setSerialNumberDefective(newSerialNumber.getDefectiveSerialNumber());
                partDetailRepo.save(partDetail);
            }
        }

        // --- BƯỚC 2: GỌI PART SERVICE (RA KHỎI VÒNG LẶP) ---
        try {
            // A. Chuẩn bị danh sách hàng hỏng trả về
            List<CompleteAllocationRequest.ReturnedPart> returnedParts = new ArrayList<>();

            for (ClaimPartDetail detail : claim.getPartDetails()) {
                if (detail.getSerialNumberDefective() != null && !detail.getSerialNumberDefective().isEmpty()) {
                    returnedParts.add(new CompleteAllocationRequest.ReturnedPart(
                            detail.getPartNumber(), // partType
                            detail.getQuantityRequired()
                    ));
                }
            }

            // B. Tạo Request
            CompleteAllocationRequest completeRequest = CompleteAllocationRequest.builder()
                    .claimCode(claim.getClaimCode())
                    .returnedParts(returnedParts)
                    .build();

            // C. Gọi sang Part Service
            partClient.completeAllocation(completeRequest);

        } catch (Exception e) {
            log.error("⚠️ Lỗi hoàn tất cấp phát/trả hàng (Part Service): {}", e.getMessage());
        }

        // --- BƯỚC 3: HOÀN TẤT CLAIM ---
        claim.setCurrentStatus(ClaimStatus.COMPLETED);
        claimRepo.save(claim);

        logRepo.save(ClaimStatusLog.builder()
                .claim(claim)
                .timestamp(LocalDateTime.now())
                .status(ClaimStatus.COMPLETED)
                .processorId(claim.getTechnicalStaffId())
                .notes("Công tác bảo hành đã hoàn thành.")
                .build());

        // --- BƯỚC 4: GỬI THÔNG BÁO FCM CHO SC STAFF (MỚI THÊM) ---
        // (Đặt cuối cùng để đảm bảo mọi thứ xong xuôi mới báo)
        try {
            Long scStaffId = claim.getScStaffId(); // Lấy ID người tạo phiếu (SC Staff)

            if (scStaffId != null) {
                // 1. Gọi User Service để lấy Token của SC Staff
                UserResponseDto scStaff = userClient.getScStaffById(scStaffId);

                // 2. Gửi thông báo
                if (scStaff != null && scStaff.getFcmToken() != null) {
                    fcmService.sendNotification(
                            scStaff.getFcmToken(),
                            "Sửa chữa hoàn tất! ✅",
                            "KTV đã xử lý xong phiếu " + claim.getClaimCode() + ". Vui lòng kiểm tra."
                    );
                    log.info("🔔 Đã gửi thông báo hoàn thành cho SC Staff: {}", scStaff.getFullName());
                } else {
                    log.warn("⚠️ SC Staff chưa có Token hoặc User null, bỏ qua gửi thông báo.");
                }
            }
        } catch (Exception e) {
            // Chỉ log lỗi, không làm ảnh hưởng luồng chính
            log.error("❌ Lỗi gửi thông báo FCM (SC Staff): {}", e.getMessage());
        }
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
    public PageCacheDto<ClaimStatusLogDto> getClaimStatusHistory(String claimCode) {
        // Sử dụng phương thức findByClaimId trong LogRepo
        List<ClaimStatusLog> logs = logRepo.findByClaim_ClaimCodeOrderByTimestampAsc(claimCode);
        // Chuyen thanh Dto:
        List<ClaimStatusLogDto> logsDto = logs.stream()
                .map(ClaimMapper::mapToLogDto)
                .toList();
        // Lay danh sach processorId
        Set<Long> proIdList = logs.stream().map(ClaimStatusLog::getId).collect(Collectors.toSet());
        // Tao Map cho UserResponse den processorId:
        Map<Long, UserResponseDto> proDetailsMapId = new HashMap<>();

        if (!proIdList.isEmpty()) {
            try {
                // *** Tối ưu: gọi userClient 1 lần (assume userClient.getUserDetailsMap(List<Long>) returns Map<Long, UserResponseDto>)
                proDetailsMapId = userClient.getUserDetailsMap(new ArrayList<>(proIdList));
                if (proDetailsMapId == null) proDetailsMapId = Collections.emptyMap();
            } catch (Exception ex) {
                // nếu feign lỗi, đừng ném, chỉ log — ta vẫn trả dữ liệu cơ bản
                log.warn("Failed to fetch processor details for claim {}: {}", claimCode, ex.toString());
                proDetailsMapId = Collections.emptyMap();
            }
        }

        final Map<Long, UserResponseDto> proDetailsMapIdFinal = proDetailsMapId;
        logsDto.forEach(d -> {
            if (d.getProcessorId() != null) {
                UserResponseDto u = proDetailsMapIdFinal.get(d.getProcessorId());
                d.setProcessorName(u != null ? u.getFullName() : "ID: " + d.getProcessorId() + " (Không tìm thấy)");
            } else {
                d.setProcessorName("Hệ thống");
            }
        });

        // 5) Wrap vào Page (unpaged) và chuyển sang PageCacheDto
        Page<ClaimStatusLogDto> page = new PageImpl<>(logsDto); // unpaged: page number = 0, size = dtos.size()
        return PageCacheDto.from(page);
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
                    specification = specification.and(WarrantyClaimSpecification.hasTechinicianId(currentUserId));
                }
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