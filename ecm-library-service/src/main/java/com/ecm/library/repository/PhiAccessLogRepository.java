package main.java.com.ecm.library.repository;

import com.ecm.library.entity.PhiAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

/**
 * Repository for PhiAccessLog.
 *
 * <p>This is an append-only table. There are intentionally no delete or
 * update methods. All query methods are read-only and designed for
 * the audit log browser in ecm-admin.</p>
 */
@Repository
public interface PhiAccessLogRepository extends JpaRepository<PhiAccessLog, UUID> {

    Page<PhiAccessLog> findByDocumentIdOrderByAccessedAtDesc(UUID documentId, Pageable pageable);

    Page<PhiAccessLog> findByAccessedByUserIdOrderByAccessedAtDesc(String userId, Pageable pageable);

    Page<PhiAccessLog> findByAccessedAtBetweenOrderByAccessedAtDesc(
            Instant from, Instant to, Pageable pageable);

    Page<PhiAccessLog> findByDocumentNumberOrderByAccessedAtDesc(
            String documentNumber, Pageable pageable);
}
