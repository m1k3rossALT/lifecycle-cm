package com.ecm.common.event;

import com.ecm.common.enums.LifecycleState;

/**
 * Fired after every successful lifecycle state transition, once the
 * lifecycle_transition audit record has been persisted.
 *
 * <p>Consumers:</p>
 * <ul>
 *   <li>WorkflowEventListener — checks if the new state triggers the next workflow step</li>
 *   <li>RetentionScheduleService — on transition to ARCHIVED, computes the retention schedule</li>
 * </ul>
 *
 * <p>The event carries both from and to states so consumers can filter on specific
 * transitions without needing to look up document state again.</p>
 */
public interface LifecycleTransitionEvent extends DomainEvent {

    /** State the document was in before this transition. */
    LifecycleState getFromState();

    /** State the document is in after this transition. */
    LifecycleState getToState();

    /**
     * User ID or system identifier that initiated the transition.
     * Will be "SYSTEM" for transitions triggered by the Retention Engine
     * or other automated processes.
     */
    String getInitiatedByUserId();

    /** Optional note recorded at the time of the transition. May be null. */
    String getTransitionNote();
}
