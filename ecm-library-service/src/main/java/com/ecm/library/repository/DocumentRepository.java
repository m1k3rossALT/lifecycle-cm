package main.java.com.ecm.library.repository;

import com.ecm.common.enums.LifecycleState;
import com.ecm.library.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Document.
 *
 * <p>Extends JpaSpecificationExecutor to support the dynamic EAV-based search
 * queries built by DocumentSearchService. Attribute-filtered searches cannot
 * be expressed as simple derived query methods because they join to
 * document_attribute with runtime-specified keys and values.</p>
 */
@Repository
public interface DocumentRepository
        extends JpaRepository<Document, UUID>, JpaSpecificationExecutor<Document> {

    Optional<Document> findByDocumentNumber(String documentNumber);

    /** Used to detect document number collisions on ingestion (should never happen with seq). */
    boolean existsByDocumentNumber(String documentNumber);

    List<Document> findByDocumentClassIdAndLifecycleState(UUID documentClassId, LifecycleState state);

    Page<Document> findByDocumentClassIdAndCurrentVersionTrue(UUID documentClassId, Pageable pageable);

    /**
     * Returns all documents in ARCHIVED state without a computed retention schedule.
     * Used by the Retention Engine's first nightly scan pass.
     */
    @Query("""
        SELECT d FROM Document d
        WHERE d.lifecycleState = 'ARCHIVED'
          AND d.currentVersion = TRUE
          AND NOT EXISTS (
              SELECT 1 FROM RetentionSchedule rs WHERE rs.document.id = d.id
          )
        """)
    List<Document> findArchivedWithoutRetentionSchedule();

    /**
     * Generates the next document number from the database sequence.
     * Called once per ingestion by DocumentService.
     */
    @Query(value = "SELECT nextval('document_number_seq')", nativeQuery = true)
    long nextDocumentNumberSequence();
}
