package main.java.com.ecm.library.repository;

import com.ecm.library.entity.LegalHoldDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LegalHoldDocumentRepository extends JpaRepository<LegalHoldDocument, UUID> {

    List<LegalHoldDocument> findByLegalHoldId(UUID legalHoldId);

    List<LegalHoldDocument> findByDocumentId(UUID documentId);

    void deleteByLegalHoldIdAndDocumentId(UUID legalHoldId, UUID documentId);
}
