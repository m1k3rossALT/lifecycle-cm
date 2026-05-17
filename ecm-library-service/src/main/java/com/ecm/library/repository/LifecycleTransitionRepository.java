package main.java.com.ecm.library.repository;

import com.ecm.common.enums.LifecycleState;
import com.ecm.library.entity.LifecycleTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for LifecycleTransition.
 *
 * <p>This table is append-only. There are intentionally no update or delete
 * methods here, and the entity itself is annotated @Immutable. Any attempt to
 * call save() on a modified transition will be silently ignored by Hibernate.</p>
 *
 * <p>findByDocumentIdOrderByCreatedAtAsc is the audit trail query — it returns
 * the complete processing history of a document in chronological order.</p>
 */
@Repository
public interface LifecycleTransitionRepository extends JpaRepository<LifecycleTransition, UUID> {

    /** Full chronological audit trail for a document. */
    List<LifecycleTransition> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);

    /** Last N transitions — used for recent activity summaries. */
    List<LifecycleTransition> findTop10ByDocumentIdOrderByCreatedAtDesc(UUID documentId);

    /** Check if a document has ever been in a given state. */
    boolean existsByDocumentIdAndToState(UUID documentId, LifecycleState toState);
}
