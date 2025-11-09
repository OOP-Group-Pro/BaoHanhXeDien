package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.ClaimDto;
import com.oem.evwarranty.enums.ClaimStatus;
import com.oem.evwarranty.mapper.ClaimMapper;
import com.oem.evwarranty.model.WarrantyClaim;
import com.oem.evwarranty.repository.WarrantyClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.oem.evwarranty.mapper.ClaimMapper.mapToClaimDto;

@Service
public class ForVehicleService {

    @Autowired
    private WarrantyClaimRepository claimRepo;

    public List<ClaimDto> getClaimsByStatus(String status) {
        ClaimStatus claimStatus = ClaimStatus.valueOf(status);
        List<WarrantyClaim> warrantyClaimList = claimRepo.getByCurrentStatus(claimStatus);
        if (warrantyClaimList.isEmpty()) {
            return null;
        }
        List<ClaimDto> claimDtoList = warrantyClaimList.stream()
                .map(ClaimMapper::mapToClaimDto)
                .toList();
        return claimDtoList;
    }
}
