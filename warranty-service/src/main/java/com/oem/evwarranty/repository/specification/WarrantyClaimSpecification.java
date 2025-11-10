package com.oem.evwarranty.repository.specification;

import com.oem.evwarranty.enums.ClaimStatus;
import com.oem.evwarranty.model.WarrantyClaim;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class WarrantyClaimSpecification {

    public static Specification<WarrantyClaim> hasClaimCode(String claimCode) {
        return (root, query, cb) ->
                claimCode == null ?  null : cb.equal(root.get("claimCode"), claimCode);
    }

    public static Specification<WarrantyClaim> hasStatus (ClaimStatus status) {
        return (root, query, cb) ->
                status == null ?  null : cb.equal(root.get("currentStatus"), status);
    }

    public static Specification<WarrantyClaim> hasVin (String vin) {
        return (root, query, cb) ->
                vin == null ?  null : cb.equal(root.get("vin"), vin);
    }

    public static Specification<WarrantyClaim> createdAfter (LocalDateTime fromDate) {
        return (root, query, cb) ->
                fromDate == null ?  null : cb.greaterThan(root.get("dateCreated"), fromDate);
    }

    public static Specification<WarrantyClaim> createdBefore (LocalDateTime toDate) {
        return (root, query, cb) ->
                toDate == null ?  null : cb.lessThan(root.get("dateCreated"), toDate);
    }

    public static Specification<WarrantyClaim> hasCenterId(Long centerId) {
        return (root, query, cb) ->
                centerId == null ?  null : cb.equal(root.get("centerId"), centerId);
    }

    public static Specification<WarrantyClaim> hasStaffId(Long staffId) {
        return (root, query, cb) ->
                staffId == null ?  null : cb.equal(root.get("scStaffId"), staffId);
    }

    public static Specification<WarrantyClaim> hasTechinicianId(Long techinicianId) {
        return (root, query, cb) ->
                techinicianId == null ?  null : cb.equal(root.get("technicalStaffId"), techinicianId);
    }
}
