package com.oem.evpart.services;

import com.oem.evpart.dto.request.ClaimAllocationRequest;
import com.oem.evpart.dto.request.CompleteAllocationRequest;
import com.oem.evpart.dto.request.PartAllocationRequest;
import com.oem.evpart.dto.response.PageCacheDto;
import com.oem.evpart.dto.response.PartAllocationResponse;
import com.oem.evpart.dto.response.PartAllocationStatusDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PartAllocationService {

    /**
     * Tạo một yêu cầu phân bổ phụ tùng.
     * Thao tác này sẽ làm giảm số lượng tồn kho tương ứng.
     */
    PartAllocationResponse createAllocation(PartAllocationRequest request);

    /**
     * Phân bổ phụ tùng cho một Claim cụ thể.
     */
    List<PartAllocationResponse> allocateForClaim(ClaimAllocationRequest request);

    /**
     * Lấy thông tin một lần phân bổ cụ thể bằng ID.
     */
    PartAllocationResponse getAllocationById(Long allocationId);

    /**
     * Lấy lịch sử phân bổ cho một trung tâm dịch vụ.
     */
    List<PartAllocationResponse> getAllocationsByServiceCenter(Integer serviceCenterId);

    /**
     * Lấy lịch sử phân bổ từ một kho hàng cụ thể.
     */
    List<PartAllocationResponse> getAllocationsByInventoryId(Long inventoryId);

    /**
     * Lấy trạng thái cấp phát phụ tùng cho một Claim cụ thể.
     * Dùng cho màn hình Kỹ thuật viên để hiển thị tiến độ.
     */
    PartAllocationStatusDto getStatusByClaimCode(String claimCode);

    PageCacheDto<PartAllocationResponse> getAllAllocations(Pageable pageable);

    public void completeAllocation(CompleteAllocationRequest request);
}
