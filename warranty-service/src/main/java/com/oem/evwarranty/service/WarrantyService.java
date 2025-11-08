package com.oem.evwarranty.service;

import com.oem.evwarranty.model.utils.UserResponseDto;
import com.oem.evwarranty.client.warranty.UserServiceClient;
import com.oem.evwarranty.dto.ClaimDto;
import com.oem.evwarranty.dto.ClaimRepairResultDto;
import com.oem.evwarranty.dto.ClaimStatusLogDto;
import com.oem.evwarranty.dto.CreateClaimDto;
import com.oem.evwarranty.enums.ClaimStatus;
import com.oem.evwarranty.mapper.ClaimMapper;
import com.oem.evwarranty.model.ClaimPartDetail;
import com.oem.evwarranty.model.ClaimStatusLog;
import com.oem.evwarranty.model.WarrantyClaim;
import com.oem.evwarranty.model.utils.PartAllocationRequest;
import com.oem.evwarranty.model.utils.SerialUpdateDetail;
import com.oem.evwarranty.client.warranty.VehicleServiceClient; // Feign Client
import com.oem.evwarranty.client.warranty.PartServiceClient;    // Feign Client

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

        // BƯỚC 2: MAP DTO SANG ENTITY VÀ LƯU
        WarrantyClaim newClaim = // Giả sử đã có Mapper để chuyển DTO sang Entity
                WarrantyClaim.builder()
                        .claimCode(generateClaimCode()) // Hàm tự tạo mã claim
                        .vin(dto.getVin())
                        .scStaffId(scStaffId) // ID nhân viên tạo
                        .description(dto.getDescription())
                        .currentStatus(ClaimStatus.WAITING_APPROVAL)
                        .dateCreated(LocalDateTime.now())
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
    public void approveClaim(Long claimId, Long evmStaffId, String approvalNotes) {
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
        claim.setTechnicalStaffId(evmStaffId);  // Lưu Chuyên viên sửa chữa
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
        UserResponseDto userResponseDto = userClient.getScStaffById(claim.getScStaffId());
        //Bước 5 chỉ thực hiện sau khi đã biết api của bên part-service cho nên hiện tại chưa dùng
        // Tao request cap phat:
        PartAllocationRequest partAllocationRequest = new PartAllocationRequest(
                claimId,
                partNumbers,
                userResponseDto.getServiceCenterId(),
                userResponseDto.getUserId()
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

    // 4. Chức năng: XEM CHI TIẾT CLAIM
    @Transactional(readOnly = true)
    public ClaimDto getClaimDetails(Long claimId) {
        WarrantyClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim không tồn tại."));

        // mapper để chuyển Entity sang DTO
        ClaimDto claimDto = mapToClaimDto(claim);
        // Lấy tên customer:
        String customerName = vehicleClient.getCustomerNameByVin(claim.getVin());
        claimDto.setCustomerName(customerName);

        return claimDto;
    }

    // 5. Chức năng: XEM LỊCH SỬ TRẠNG THÁI
    @Transactional(readOnly = true)
    public List<ClaimStatusLogDto> getClaimStatusHistory(Long claimId) {
        // Sử dụng phương thức findByClaimId trong LogRepo
        List<ClaimStatusLog> logs = logRepo.findByClaim_IdOrderByTimestampAsc(claimId);

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

    public List<ClaimDto> getAllClaims() {
        return claimRepo.findAll().stream()
                .map(ClaimMapper::mapToClaimDto)
                .collect(Collectors.toList());
    }
}
