package main.java.com.ecm.library.entity;

import com.ecm.common.enums.LifecycleState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A single step in a workflow definition.
 *
 * <p>WorkflowEventListener scans the steps of a workflow to find which one
 * has a triggersOnState matching the document's new lifecycle state, then
 * creates a WorkflowTask for that step.</p>
 *
 * <p>SLA values are stored here and can be adjusted by administrators in
 * the admin console without code changes.</p>
 *
 * <p>Not responsible for: creating tasks or computing due_at timestamps —
 * those are owned by WorkflowService.</p>
 */
@Entity
@Table(name = "workflow_step")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    /** The lifecycle state that causes this step's task to be created. */
    @Enumerated(EnumType.STRING)
    @Column(name = "triggers_on_state", nullable = false, length = 30)
    private LifecycleState triggersOnState;

    @Column(name = "assigned_role_code", nullable = false, length = 50)
    private String assignedRoleCode;

    /** Standard SLA in hours from task creation to due_at. */
    @Column(name = "sla_hours", nullable = false)
    private int slaHours;

    /**
     * Multiplier applied to sla_hours for URGENT documents.
     * 0.25 means urgent SLA is 25% of standard (e.g. 6h instead of 24h).
     */
    @Column(name = "urgency_sla_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal urgencySlaMultiplier = BigDecimal.ONE;

    /** The lifecycle state the document transitions to when this step is completed. */
    @Enumerated(EnumType.STRING)
    @Column(name = "resolves_to_state", nullable = false, length = 30)
    private LifecycleState resolvesToState;
}
