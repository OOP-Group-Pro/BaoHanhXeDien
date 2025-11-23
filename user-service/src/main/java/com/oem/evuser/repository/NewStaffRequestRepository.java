package com.oem.evuser.repository;

import com.oem.evuser.entity.NewStaffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewStaffRequestRepository extends JpaRepository<NewStaffRequest, Long> {
    List<NewStaffRequest> findByStatus(String status);
    List<NewStaffRequest> findByCreatedBy(String createdBy);
}
