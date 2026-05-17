package main.java.com.ecm.library.repository;

import com.ecm.library.entity.RetentionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RetentionSchedule.
 *
 * <p>The two nightly scan queries are the primary access patterns here —
 * the Retention Engine uses them to find documents needing schedule
 * computation and documents past their eligibility date.</p>
 */
@Repository
public interface RetentionScheduleRepository extends JpaRepository<RetentionSchedule, UUID> {

    Optional<RetentionSchedule> findByDocumentId(UUID documentId);

    /**
     * Nightly scan pass 2: scheduled documents whose eligibility date has
     * passed. The engine then checks legal holds and flags as ELIGIBLE
     * or ON_LEGAL_HOLD accordingly.
     */
    @Query("""
        SELECT rs FROM RetentionSchedule rs
        WHERE rs.status = 'SCHEDULED'
          AND rs.eligibleForDispositionDate <= :today
        """)
    List<RetentionSchedule> findScheduledDocumentsPastEligibilityDate(LocalDate today);
}
