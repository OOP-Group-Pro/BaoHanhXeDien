package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.*;
import com.oem.evwarranty.mapper.PartMapper;
import com.oem.evwarranty.model.utils.PartResponseDto;
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
import com.oem.evwarranty.repository.specification.WarrantyClaimSpecification;
import com.oem.evwarranty.security.UserDetailsPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oem.evwarranty.mapper.ClaimMapper.mapToClaimDto;
import static com.oem.evwarranty.mapper.PartMapper.mapToPartDetail;

import com.oem.evwarranty.repository.ClaimPartDetailRepository;
import com.oem.evwarranty.repository.ClaimStatusLogRepository;
import com.oem.evwarranty.repository.WarrantyClaimRepository;


@Service
public class WarrantyService {

    // 1. Dependencies Nội bộ (Repositories)
    private final WarrantyClaimRepository claimRepo;
    private final ClaimStatusLogRepository logRepo;
    private final ClaimPartDetailRepository partDetailRepo;
    // ... (Các Repository khác: ClaimCostRepository, AttachedDocumentRepository)

    // 2. Dependencies Liên Service (Feign Clients)
    private final VehicleServiceClient vehicleClient;
    private final PartServiceClient partClient;
    private final UserServiceClient userClient;

    // Constructor Injection (Spring Boot tự động tiêm các dependency này)
    public WarrantyService(
            WarrantyClaimRepository claimRepo,
            ClaimStatusLogRepository logRepo,
            ClaimPartDetailRepository partDetailRepo,
            VehicleServiceClient vehicleClient,
            PartServiceClient partClient,
            UserServiceClient userClient) {

        this.claimRepo = claimRepo;
        this.logRepo = logRepo;
        this.partDetailRepo = partDetailRepo;
        this.vehicleClient = vehicleClient;
        this.partClient = partClient;
        this.userClient = userClient;
    }

    // --- BẮT ĐẦU CÁC PHƯƠNG THỨC NGHIỆP VỤ ---

    // 1. Chức năng: TẠO CLAIM MỚI
    @Transactional
    public Long createClaim(CreateClaimDto dto, Long scStaffId) {
        // [Logic chính]:
        // 1. Xác thực VIN (Gọi Vehicle-Service).
        // 2. Map DTO sang Entity.
        // 3. Lưu Claim, ClaimPartDetail, ClaimStatusLog.

        // BƯỚC 1: XÁC THỰC - GỌI SERVICE NGOÀI
        // Giả sử client trả về TRUE nếu VIN hợp lệ
        boolean isVinValid = vehicleClient.validateVin(dto.getVin());
        if (!isVinValid) {
            throw new IllegalArgumentException("VIN không hợp lệ hoặc không tồn tại.");
        }

        UserResponseDto userInfo = userClient.getScStaffById(scStaffId);

        // BƯỚC 2: MAP DTO SANG ENTITY VÀ LƯU
        WarrantyClaim newClaim = // Giả sử đã có Mapper để chuyển DTO sang Entity
                WarrantyClaim.builder()
                        .claimCode(generateClaimCode()) // Hàm tự tạo mã claim
                        .vin(dto.getVin())
                        .scStaffId(scStaffId) // ID nhân viên tạo
                        .centerId(userInfo.getServiceCenterId())
                        .description(dto.getDescription())
                        .currentStatus(ClaimStatus.WAITING_APPROVAL)
                        .dateCreated(LocalDateTime.now())
                        .technicalStaffId(null)
                        .build();

        claimRepo.save(newClaim);

        // BƯỚC 3: LƯU CÁC THỰC THỂ PHỤ
        // 3a. Lưu Chi tiết Phụ tùng (Part Details)
        // Truoc het can lay danh sach cac phu tùng bị hư đề xuat bảo hành:

        partDetailRepo.saveAll(dto.getRequestedParts().stream().map(d -> mapToPartDetail(d, newClaim)).toList());

        // 3b. Ghi lại Log trạng thái đầu tiên
        logRepo.save(
                ClaimStatusLog.builder()
                        .claim(newClaim)
                        .timestamp(LocalDateTime.now())
                        .status(ClaimStatus.WAITING_APPROVAL)
                        .processorId(scStaffId) // Người tạo chính là người xử lý ban đầu
                        .notes("Yêu cầu bảo hành được khởi tạo.")
                        .build());

        return newClaim.getId();
    }


    // 2. Chức năng: PHÊ DUYỆT CLAIM (EVM STAFF)
    @Transactional
    public void approveClaim(Long claimId, Long evmStaffId, String approvalNotes, Long technicalStaffId) {
        // [Logic chính]:
        // 1. Kiểm tra trạng thái hiện tại (phải là WAITING_APPROVAL).
        // 2. Cập nhật trạng thái Claim.
        // 3. Cập nhật trạng thái phê duyệt cho ClaimPartDetail.
        // 4. Ghi Log.
        // 5. Yêu cầu cấp phát phụ tùng (Gọi Part-Service).
        /*  ### Chu y: O day,   */
        WarrantyClaim claim = claimRepo.findById(claimId)
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
        List<ClaimPartDetail> parts = partDetailRepo.findAllByClaim_Id(claimId);
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

        // BƯỚC 5: GỌI SERVICE NGOÀI - YÊU CẦU CẤP PHÁT PHỤ TÙNG
        // De yeu cau cap phat, can biet Ai, noi nao yeu cau cap phat :
        //Bước 5 chỉ thực hiện sau khi đã biết api của bên part-service cho nên hiện tại chưa dùng
        // Tao request cap phat:
        PartAllocationRequest partAllocationRequest = new PartAllocationRequest(
                claimId,
                partNumbers,
                claim.getCenterId(),
                claim.getScStaffId()
        );
        // Gui yeu cau
        partClient.requestPartAllocation(partAllocationRequest);
    }

    public void rejectClaim(Long claimId, Long evmStaffId, String reason) {
        // B1: Xac dinh claim bi tu choi
        // B2: Kiem tra co reject claim duoc hay khong -> kiem tra trang thai claim -> Chi co the reject neu claim dang trong status WAITING_APPROVAL
        // B3: Reject -> cap nhat currentStatus = ClaimStatus.REJECTED -> Luu lai: claimRepo.save(claim)
        // B4: Ghi vao log
        WarrantyClaim claim = claimRepo.findById(claimId).orElseThrow(() -> new IllegalArgumentException("Claim không tồn tại"));

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
    public void updateRepairResult(Long claimId, ClaimRepairResultDto resultDto) {
        // [Logic chính]:
        // 1. Kiểm tra trạng thái Claim (Phải là APPROVED/IN_PROGRESS).
        // 2. Cập nhật số seri mới lắp/hỏng cho ClaimPartDetail.
        // 3. Cập nhật trạng thái Claim thành COMPLETED (nếu tất cả serial được điền).

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
    public ClaimDto getClaimDetails(String claimCode) {
        WarrantyClaim claim = claimRepo.findByClaimCode(claimCode)
                .orElseThrow(() -> new IllegalArgumentException("Claim không tồn tại."));

        // 1. Chuyển đổi cơ bản (dùng Mapper)
        ClaimDto claimDto = ClaimMapper.mapToClaimDto(claim);

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

        // 4b. Gọi API Feign 1 LẦN DUY NHẤT để lấy Map
        Map<String, PartResponseDto> partDetailsMap = Map.of(); // Map rỗng
        if (!partNumbers.isEmpty()) {
            try {
                partDetailsMap = partClient.getPartsByNumbers(partNumbers);
            } catch (Exception e) {
                // log.error("Không thể lấy chi tiết phụ tùng: {}", e.getMessage());
            }
        }

        // 4c. Map vào DTO
        final Map<String, PartResponseDto> finalPartMap = partDetailsMap; // (Cần cho Lambda)

        List<ClaimPartDetailDto> partDtos = claim.getPartDetails().stream()
                .map(entity -> {
                    // Dùng PartMapper cũ
                    ClaimPartDetailDto dto = PartMapper.mapToClaimPartDetailDto(entity);

                    // Lấy PartResponse từ Map
                    PartResponseDto partInfo = finalPartMap.get(entity.getPartNumber());

                    // Gán partName (NẾU TÌM THẤY)
                    if (partInfo != null) {
                        dto.setPartName(partInfo.getName()); // Giả sử hàm là .getName()
                    } else {
                        dto.setPartName("(Không tìm thấy tên part)");
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        claimDto.setPartList(partDtos);

        // 5. Lấy Lịch sử (Chúng ta sẽ gọi riêng từ frontend)
        // (Bỏ qua statusHistory ở đây để frontend gọi getClaimHistory)

        return claimDto;
    }

    // 5. Chức năng: XEM LỊCH SỬ TRẠNG THÁI
    @Transactional(readOnly = true)
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
    public Page<ClaimDto> getClaims(
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

        // Lay Principal tu SecurityContext
        Object principal = authentication.getPrincipal();
        // Ap dung logic phan quyen theo loai Principal:
        if (principal instanceof UserDetailsPrincipal userDetails) {

            Long currentUserId = userDetails.getUserId();
            Long currentCenterId = userDetails.getCenterId();

            List<String> roles = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .toList();

            if (!roles.contains("ROLE_ADMIN") && ! roles.contains("ROLE_SC_TECHNICIAN") && ! roles.contains("ROLE_EVM_STAFF")) {
                if (roles.contains("ROLE_MANAGER")) {
                    specification = specification.and(WarrantyClaimSpecification.hasCenterId(currentCenterId));
                }
                else specification = specification.and(WarrantyClaimSpecification.hasStaffId(currentUserId));
            }
        } else if (principal instanceof String && principal.equals("internal-service")) {
        } else {
            // Case C: Lỗi bảo mật không xác định
            throw new AccessDeniedException("Cannot determine user identity or authorization context.");
        }

        // de toi uu viec goi feign và goi api thì thay vì mỗi một claim gọi một lần,
        // Thì lấy danh sách những cái cần gọi, sau đó tạo feign yêu cầu truy vấn theo danh sách và
        // Trả về thông tin cần theo danh sách đó

        // Có 2 thông tin cần feign về:
        // 1 là userDetail từ user service
        // 2 là customer name từ VIN của vehicle service (Chu y, cafn loc VIN trung lap truoc khi goi feign)
        // Bước làm:

        // 1: Lay ket qua tu CSDL chua goi Feign:
        Page<WarrantyClaim> claimPage = claimRepo.findAll(specification, pageable);

        // Lay danh sach technician ma claim da co:
        List<Long> technicianIds = claimPage.getContent().stream()
                .map(WarrantyClaim::getTechnicalStaffId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        // Lay danh sach cac VIN (loai bo trung lap)
        List<String> vins = claimPage.getContent().stream()
                .map(WarrantyClaim::getVin)
                .distinct()
                .toList();
        // Goi 2 feign de lay cac thong tin theo danh sach:
        Map<String, String> customerNameMap = vehicleClient.getCustomerNamesByVins(vins);
        Map<Long, UserResponseDto> technicianMap = userClient.getUserDetailsMap(technicianIds);

        // Truyen du lieu vao List cac claim Dto:
        List<ClaimDto> claimDtos = claimPage.getContent().stream()
                .map(claim -> {
                    ClaimDto dto = ClaimMapper.mapToClaimDto(claim);
                    // gan customer name vao dto:
                    dto.setCustomerName(customerNameMap.getOrDefault(dto.getVin(), "(Không tìm thấy)"));

                    if (claim.getTechnicalStaffId() != null) {
                        // Lay thong tin cua technician neu da co :
                        UserResponseDto userInfo = technicianMap.get(claim.getTechnicalStaffId());
                        if (userInfo != null) {
                            dto.setTechnicalName(userInfo.getFullName());
                        }
                    }
                    return dto;
                })
                .toList();


        // 4️⃣ Lấy page
        return new PageImpl<>(claimDtos, pageable, claimPage.getTotalElements());
    }
}