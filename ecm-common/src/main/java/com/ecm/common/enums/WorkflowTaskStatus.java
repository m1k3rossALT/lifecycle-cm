package com.ecm.common.enums;

/**
 * The current status of a single WorkflowTask.
 *
 * <p>Tasks begin as PENDING in a role queue. A user claims the task (CLAIMED),
 * then completes it (COMPLETED) or releases it back to the queue (PENDING).
 * The SLA breach scanner moves overdue tasks to ESCALATED.</p>
 *
 * <p>Not responsible for: SLA computation or escalation logic — those are owned
 * by WorkflowService and the SLA breach scheduler in ecm-library-service.</p>
 */
public enum WorkflowTaskStatus {

    /**
     * Task is in the role queue, unclaimed. Visible to all users with the
     * assigned role. Any eligible user may claim it.
     */
    PENDING,

    /**
     * Task has been claimed by a specific user. Removed from the general role
     * queue view; visible only in the claiming user's personal work list.
     * Other users cannot claim a task already claimed.
     */
    CLAIMED,

    /**
     * Task has been completed by the claiming user. The document has transitioned
     * to the next state. Terminal status — no further transitions from COMPLETED.
     */
    COMPLETED,

    /**
     * Task has breached its SLA. Priority is set to maximum. Displayed prominently
     * in the role queue with a visual indicator. A supervisor notification is
     * generated. The task remains actionable — ESCALATED is a flag, not a block.
     */
    ESCALATED,

    /**
     * Task was cancelled — for example, because the associated document was placed
     * on legal hold or moved to REMEDIATION before the task could be completed.
     * Terminal status.
     */
    CANCELLED
}
