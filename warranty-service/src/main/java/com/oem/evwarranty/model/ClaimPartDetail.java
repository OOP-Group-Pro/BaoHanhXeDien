package com.oem.evwarranty.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="claim_part_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimPartDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="claim_id")
    private WarrantyClaim claim;

    @Column(nullable = false)
    private String partNumber;      // Mã số cho loại phụ tung -> dùng cái này để kiểm tra trong chính sách bảo hành và tồn kho
    @Column(nullable = true)
    private String partName;
    @Column(nullable = false)
    private Integer quantityRequired;       // Số lượng yêu cầu
    @Column(nullable = true, unique = true)
    private String serialNumberReplace;     // Ma so serial phu tung thay the -> là số serial định danh mỗi phụ tùng
    @Column(nullable = true)
    private String serialNumberDefective;       // Ma so serial phu tung can thay the (là cái phụ tùng bị hư á)
    @Column(nullable = false)
    private Boolean isApproved;     // phụ tùng này có được duyệt bảo hành thay thế không
}
