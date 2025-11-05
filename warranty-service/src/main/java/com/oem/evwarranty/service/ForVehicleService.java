package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.ClaimDto;
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
        List<WarrantyClaim> warrantyClaimList = claimRepo.getByStatus(status);
        if (warrantyClaimList.isEmpty()) {
            return null;
        }
        List<ClaimDto> claimDtoList = warrantyClaimList.stream()
                .map(ClaimMapper::mapToClaimDto)
                .toList();
        return claimDtoList;
    }
}
