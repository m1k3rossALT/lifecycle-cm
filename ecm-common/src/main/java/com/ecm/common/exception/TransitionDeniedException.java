package com.ecm.common.exception;

import com.ecm.common.enums.LifecycleState;

/**
 * Thrown when a lifecycle transition is rejected by the state machine guard.
 *
 * <p>A transition can be denied for several reasons; the {@code denyReason} field
 * captures which of the five guard checks failed. This reason is included in the
 * API response body and in the audit log entry for the denied transition attempt.</p>
 *
 * <p>Maps to HTTP 403 Forbidden in the global exception handler.</p>
 */
public class TransitionDeniedException extends EcmException {

    private static final String ERROR_CODE = "TRANSITION_DENIED";

    /**
     * Machine-readable reason codes, matching the five guard checks evaluated
     * before any lifecycle transition is permitted.
     */
    public enum DenyReason {
        /** The from→to transition does not exist in the state machine definition. */
        ILLEGAL_TRANSITION,
        /** The requesting user's role does not have can_transition for this document class. */
        ROLE_NOT_PERMITTED,
        /** The specific from→to transition is not in the role's permitted transition list. */
        TRANSITION_NOT_PERMITTED_FOR_ROLE,
        /** The target state is DISPOSED and the user does not have can_dispose permission. */
        DISPOSE_NOT_PERMITTED,
        /** An active legal hold is blocking this transition. */
        LEGAL_HOLD_ACTIVE
    }

    private final LifecycleState fromState;
    private final LifecycleState toState;
    private final DenyReason denyReason;

    public TransitionDeniedException(String documentNumber,
                                     LifecycleState fromState,
                                     LifecycleState toState,
                                     DenyReason denyReason,
                                     String correlationId) {
        super(ERROR_CODE,
              String.format("Transition denied for document %s: %s → %s [reason: %s]",
                            documentNumber, fromState, toState, denyReason),
              correlationId);
        this.fromState = fromState;
        this.toState = toState;
        this.denyReason = denyReason;
    }

    public LifecycleState getFromState() {
        return fromState;
    }

    public LifecycleState getToState() {
        return toState;
    }

    public DenyReason getDenyReason() {
        return denyReason;
    }
}
