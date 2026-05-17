package com.ecm.common.enums;

/**
 * Represents every possible position a document can occupy in its lifecycle.
 *
 * <p>A document is always in exactly one state. Transitions between states are
 * enforced by the Spring Statemachine in ecm-library-service; illegal transitions
 * are structurally impossible, not just conditionally blocked.</p>
 *
 * <p>Not responsible for: defining which roles may trigger which transitions —
 * that logic lives in the statemachine guard configuration.</p>
 */
public enum LifecycleState {

    /**
     * Document has entered the system but has not yet been validated, indexed,
     * or assigned. Initial state for all ingested documents.
     */
    RECEIVED("Received — awaiting indexing", false),

    /**
     * A processor has reviewed the document, confirmed its identity, verified
     * its attributes, and confirmed the correct Document Class. First-class
     * repository object.
     */
    INDEXED("Indexed — attributes verified", false),

    /**
     * Document is actively being evaluated for a business decision (adjudication,
     * clinical review, etc.).
     */
    UNDER_REVIEW("Under Review — active evaluation in progress", false),

    /**
     * Business decision made affirmatively. Claim approved for payment;
     * authorization approved for service.
     */
    APPROVED("Approved — affirmative decision recorded", false),

    /**
     * Business decision made negatively. Document closed to standard processing;
     * may become subject to an appeal.
     */
    DENIED("Denied — negative decision recorded", false),

    /**
     * Processing paused pending additional information from member, provider,
     * or internal source. Remains in active queue.
     */
    PENDED("Pended — awaiting additional information", false),

    /**
     * Business process complete. Document is no longer active but must be retained
     * per its governing retention policy. This state triggers retention schedule
     * computation in the Retention Engine.
     */
    ARCHIVED("Archived — in governed retention", false),

    /**
     * The Retention Engine has determined that the document has satisfied its full
     * retention obligation and is eligible to be disposed. System-assigned only —
     * no human role may place a document in this state directly.
     */
    ELIGIBLE_FOR_DISPOSITION("Eligible for Disposition — retention satisfied", false),

    /**
     * Document content has been destroyed per the governing retention policy and
     * a Compliance Officer approval. Metadata, attribute values, lifecycle history,
     * and audit trail are permanently preserved. Terminal state — no further
     * transitions are permitted.
     */
    DISPOSED("Disposed — content destroyed, metadata preserved", true),

    /**
     * An active legal hold has been placed on this document by a Compliance Officer.
     * Transition to ELIGIBLE_FOR_DISPOSITION is blocked regardless of retention
     * schedule. Released by the Compliance Officer when the hold reason is resolved.
     */
    ON_LEGAL_HOLD("On Legal Hold — disposition blocked", false),

    /**
     * Document has been flagged as requiring correction: indexing error, wrong
     * Document Class assignment, or invalid attribute values. Returned to a
     * processor for reprocessing.
     */
    REMEDIATION("Remediation — requires correction before processing", false);

    // -----------------------------------------------------------------------

    /** Human-readable description for display and audit log output. */
    private final String description;

    /**
     * True if this is a terminal state — no further transitions are permitted
     * once a document reaches a terminal state.
     */
    private final boolean terminal;

    LifecycleState(String description, boolean terminal) {
        this.description = description;
        this.terminal = terminal;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return terminal;
    }
}
