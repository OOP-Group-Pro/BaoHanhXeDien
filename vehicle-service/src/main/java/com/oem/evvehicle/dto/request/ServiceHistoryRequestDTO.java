package com.oem.evvehicle.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceHistoryRequestDTO {
    @NotNull
    private LocalDateTime performedDate;
    @NotBlank
    private String description;
    @NotNull
    private Long vehicleId;
    @NotNull
    private Long technicianId;
    //Có thể rỗng vì có thể ko lắp gì.
    private List<Long> partIds; // Danh sách ID của các linh kiện liên quan
    @NotNull(message = "Phải nhập số ODO tại thời điểm sửa chữa")
    private Long odometerReading;
}