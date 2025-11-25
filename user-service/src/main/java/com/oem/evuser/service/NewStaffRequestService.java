package com.oem.evuser.service;

import com.oem.evuser.entity.NewStaffRequest;
import com.oem.evuser.entity.Role;
import com.oem.evuser.entity.User;
import com.oem.evuser.entity.UserStatus;
import com.oem.evuser.repository.NewStaffRequestRepository;
import com.oem.evuser.repository.RoleRepository;
import com.oem.evuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.ObjectNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j; // ✅ Import cho Log

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewStaffRequestService {

    private final NewStaffRequestRepository requestRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    // Manager gửi phiếu
    public void createRequest(NewStaffRequest request, String createdBy) {
        request.setStatus("PENDING");
        request.setCreatedBy(createdBy);
        request.setCreatedAt(LocalDateTime.now());
        // Copy roles để tránh shared reference nếu ngoài code set trực tiếp
        if (request.getProposedRoles() != null) {
            request.setProposedRoles(new HashSet<>(request.getProposedRoles()));
        }
        requestRepo.save(request);
    }

    @Transactional
    public void approveRequest(Long requestId, String approvedBy) {
        log.info("🟢 Starting approval for request ID: {}", requestId);

        // 1. Tìm và Validate Request
        NewStaffRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request has already been processed (Status: " + request.getStatus() + ")");
        }

        // 2. Validate User & Email tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' already exists");
        }

        // 3. XỬ LÝ ROLE THÔNG MINH (FIX LỖI CỦA BẠN)
        Set<Role> roles = request.getProposedRoles();

        // Nếu bảng trung gian rỗng, thử tìm từ chuỗi String 'role'
        if (roles == null || roles.isEmpty()) {
            log.warn("⚠️ ProposedRoles is empty for reqId: {}. Trying fallback to String role...", requestId);

            String roleNameStr = request.getRole(); // Lấy chuỗi VD: "ROLE_SC_STAFF"

            if (roleNameStr != null && !roleNameStr.isEmpty()) {
                // Xử lý tên role (bỏ tiền tố ROLE_ nếu cần, tùy dữ liệu trong bảng roles)
                // Giả sử bảng roles lưu "SC_STAFF" -> cần xóa "ROLE_"
                // Giả sử bảng roles lưu "ROLE_SC_STAFF" -> giữ nguyên

                // 🟢 THỬ TÌM TRỰC TIẾP TRƯỚC
                Role fallbackRole = roleRepository.findByRoleName(roleNameStr).orElse(null);

                // 🟢 NẾU KHÔNG THẤY, THỬ BỎ "ROLE_"
                if (fallbackRole == null && roleNameStr.startsWith("ROLE_")) {
                    String shortName = roleNameStr.replace("ROLE_", "");
                    fallbackRole = roleRepository.findByRoleName(shortName).orElse(null);
                }

                if (fallbackRole != null) {
                    roles = new HashSet<>();
                    roles.add(fallbackRole);
                    log.info("✅ Fallback successful. Found role: {}", fallbackRole.getRoleName());
                } else {
                    throw new IllegalArgumentException("Role not found in DB: " + roleNameStr);
                }
            } else {
                throw new IllegalArgumentException("Cannot approve: No role specified (both list and string are empty)");
            }
        }

        // 4. Tạo User Mới
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode("123456"));
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setServiceCenterId(request.getServiceCenterId());

        // Gán Role
        newUser.setRoles(new HashSet<>(roles));

        userRepository.save(newUser);

        // 5. Cập nhật trạng thái phiếu
        request.setStatus("APPROVED");
        request.setApprovedBy(approvedBy);
        request.setApprovedAt(LocalDateTime.now());
        requestRepo.save(request);

        log.info("✅ Approved successfully. New User ID: {}", newUser.getUserId());
    }

    // Admin từ chối phiếu
    public void rejectRequest(Long requestId, String approvedBy) {
        NewStaffRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already processed");
        }

        request.setStatus("REJECTED");
        request.setApprovedBy(approvedBy);
        request.setApprovedAt(LocalDateTime.now());
        requestRepo.save(request);
    }

    // Lấy danh sách PENDING
    public List<NewStaffRequest> getPendingRequests() {
        return requestRepo.findByStatus("PENDING");
    }

    // 🟢 Manager xem các phiếu do mình tạo
    public List<NewStaffRequest> getRequestsByCreator(String createdBy) {
        return requestRepo.findByCreatedBy(createdBy);
    }

    // 🟣 Admin xem tất cả phiếu
    public List<NewStaffRequest> getAllRequests() {
        return requestRepo.findAll();
    }

    // Cập nhật phiếu
    public NewStaffRequest updateRequest(Long requestId, NewStaffRequest updatedRequest) {
        NewStaffRequest existing = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!"PENDING".equals(existing.getStatus())) {
            throw new RuntimeException("Cannot edit request that is already processed");
        }

        existing.setFullName(updatedRequest.getFullName());
        existing.setUsername(updatedRequest.getUsername());
        existing.setServiceCenterId(updatedRequest.getServiceCenterId());

        // Copy proposedRoles để tránh shared reference
        if (updatedRequest.getProposedRoles() != null) {
            existing.setProposedRoles(new HashSet<>(updatedRequest.getProposedRoles()));
        } else {
            existing.setProposedRoles(new HashSet<>());
        }

        return requestRepo.save(existing);
    }

}
