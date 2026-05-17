package main.java.com.ecm.library.entity;

import com.ecm.common.enums.WorkflowTaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * An individual work item in a role queue for a specific document.
 *
 * <p>Tasks begin as PENDING. A user claims the task (CLAIMED), then completes
 * it (COMPLETED), releases it back to PENDING, or it escalates if the SLA
 * breaches. The SLA breach scanner runs every 15 minutes and updates
 * overdue tasks to ESCALATED.</p>
 *
 * <p>Not responsible for: creating tasks (WorkflowService), SLA computation
 * (WorkflowService), or escalation scanning (WorkflowSlaScanner).</p>
 */
@Entity
@Table(name = "workflow_task")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStep workflowStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkflowTaskStatus status = WorkflowTaskStatus.PENDING;

    /**
     * Higher = shown first in role queue. Recalculated by the SLA scanner
     * based on time remaining until due_at and the document's urgency_level.
     */
    @Column(name = "priority", nullable = false)
    private int priority = 0;

    @Column(name = "assigned_role_code", nullable = false, length = 50)
    private String assignedRoleCode;

    /** Set when a user claims the task. Cleared if the task is released. */
    @Column(name = "claimed_by_user_id", length = 100)
    private String claimedByUserId;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    /** Computed at task creation: NOW() + (sla_hours * urgency_multiplier if urgent). */
    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "completed_by_user_id", length = 100)
    private String completedByUserId;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "escalated_at")
    private Instant escalatedAt;

    @Column(name = "escalation_reason")
    private String escalationReason;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
