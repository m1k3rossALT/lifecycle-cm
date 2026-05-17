package main.java.com.ecm.library.repository;

import com.ecm.library.entity.DocumentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for DocumentClass.
 *
 * <p>findByCode is the primary lookup for every document ingestion request —
 * the caller specifies a class code (e.g. MEDICAL_CLAIM) and the service
 * resolves the full class definition from it.</p>
 */
@Repository
public interface DocumentClassRepository extends JpaRepository<DocumentClass, UUID> {

    Optional<DocumentClass> findByCode(String code);

    Optional<DocumentClass> findByCodeAndActiveTrue(String code);

    List<DocumentClass> findAllByActiveTrue();
}
