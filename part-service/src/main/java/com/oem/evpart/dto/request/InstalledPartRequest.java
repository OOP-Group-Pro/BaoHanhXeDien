package com.oem.evpart.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstalledPartRequest {

    @NotBlank(message = "vehicleVin là bắt buộc")
    private String vehicleVin;

    @NotNull(message = "partId là bắt buộc")
    private Long partId;

    private String serialNumber;

    private String status; // Installed, Replaced, Removed
}

