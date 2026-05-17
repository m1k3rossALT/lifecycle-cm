package main.java.com.ecm.library.repository;

import com.ecm.common.enums.WorkflowTaskStatus;
import com.ecm.library.entity.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for WorkflowTask.
 *
 * <p>The queue and SLA scanner queries are the core access patterns here.
 * Tasks are fetched by role for the inbox view, and by due_at for
 * escalation detection.</p>
 */
@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, UUID> {

    /**
     * Role queue view: unclaimed and escalated tasks for a given role,
     * ordered by priority descending (highest priority first).
     */
    @Query("""
        SELECT wt FROM WorkflowTask wt
        WHERE wt.assignedRoleCode = :roleCode
          AND wt.status IN ('PENDING', 'ESCALATED')
        ORDER BY wt.priority DESC, wt.dueAt ASC
        """)
    List<WorkflowTask> findQueueForRole(@Param("roleCode") String roleCode);

    /** Personal work list: tasks currently claimed by a specific user. */
    List<WorkflowTask> findByClaimedByUserIdAndStatus(
            String claimedByUserId, WorkflowTaskStatus status);

    /** All tasks for a document — used when displaying document detail. */
    List<WorkflowTask> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);

    /**
     * SLA breach scanner: tasks that are past due and not yet completed or cancelled.
     * Called every 15 minutes by WorkflowSlaScanner.
     */
    @Query("""
        SELECT wt FROM WorkflowTask wt
        WHERE wt.dueAt < :now
          AND wt.status NOT IN ('COMPLETED', 'CANCELLED', 'ESCALATED')
        """)
    List<WorkflowTask> findOverdueTasks(@Param("now") Instant now);
}
