package com.oem.evpart.services;

import com.oem.evpart.dto.request.PartAllocationRequest;
import com.oem.evpart.dto.response.PartAllocationResponse;
import java.util.List;

public interface PartAllocationService {
    /**
     * Tạo một yêu cầu phân bổ phụ tùng.
     * Thao tác này sẽ làm giảm số lượng tồn kho tương ứng.
     */
    PartAllocationResponse createAllocation(PartAllocationRequest request);

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
}