package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.AttachedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachedDocumentRepository extends JpaRepository<AttachedDocument,Long> {
}
