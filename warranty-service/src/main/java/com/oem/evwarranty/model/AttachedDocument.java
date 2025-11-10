package com.oem.evwarranty.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="attached_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachedDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="claim_id")
    private WarrantyClaim claim;

    private String fileName;

    private String fileType;

    private String storagePath;

    private LocalDateTime uploadDate;
}
