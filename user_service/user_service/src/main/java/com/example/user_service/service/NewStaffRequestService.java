package com.example.user_service.service;

import com.example.user_service.entity.NewStaffRequest;
import com.example.user_service.entity.Role;
import com.example.user_service.entity.User;
import com.example.user_service.entity.UserStatus;
import com.example.user_service.repository.NewStaffRequestRepository;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NewStaffRequestService {

    private final NewStaffRequestRepository requestRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    // Admin duyệt phiếu → tạo user mới
    public void approveRequest(Long requestId, String approvedBy) {
        NewStaffRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already processed");
        }

        Set<Role> roles = request.getProposedRoles();
        if (roles == null || roles.isEmpty()) {
            throw new RuntimeException("No role proposed");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        System.out.println("Approving request ID: " + requestId);
        System.out.println("Username: " + request.getUsername());
        System.out.println("Roles: " + roles);
        System.out.println("ServiceCenter: " + request.getServiceCenterId());

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode("123456"));
        newUser.setEmail(request.getEmail() != null ? request.getEmail() : "");
        newUser.setPhone(request.getPhone() != null ? request.getPhone() : "");
        newUser.setStatus(UserStatus.ACTIVE);
        // Tạo copy mới để tránh shared reference
        newUser.setRoles(new HashSet<>(roles));
        newUser.setServiceCenterId(request.getServiceCenterId());

        userRepository.save(newUser);

        request.setStatus("APPROVED");
        request.setApprovedBy(approvedBy);
        request.setApprovedAt(LocalDateTime.now());
        requestRepo.save(request);

        System.out.println("Approved successfully!");
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
