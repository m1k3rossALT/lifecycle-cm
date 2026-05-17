package main.java.com.ecm.library.repository;

import com.ecm.library.entity.LegalHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LegalHoldRepository extends JpaRepository<LegalHold, UUID> {

    Optional<LegalHold> findByHoldIdentifier(String holdIdentifier);

    List<LegalHold> findByActiveTrue();

    /**
     * Checks whether a document has any active legal hold.
     * Used by the state machine guard before permitting disposition transitions.
     */
    @Query("""
        SELECT COUNT(lhd) > 0 FROM LegalHoldDocument lhd
        WHERE lhd.document.id = :documentId
          AND lhd.legalHold.active = TRUE
        """)
    boolean existsActiveLegalHoldForDocument(@Param("documentId") UUID documentId);
}
