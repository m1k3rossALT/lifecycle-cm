package com.ecm.common.enums;

/**
 * The business event that starts the retention clock for a document.
 *
 * <p>The Retention Engine reads the trigger event from the governing retention policy
 * and resolves the actual trigger date by looking up the corresponding attribute value
 * from the document's attribute set.</p>
 *
 * <p>Why this is not just "document creation": healthcare retention regulations are
 * explicit that the retention period begins from a business event — claim closure,
 * service date, member termination — not from when the file arrived in the system.
 * Using the wrong trigger date can cause premature or late disposition, both of which
 * are compliance violations.</p>
 *
 * <p>Not responsible for: resolving which document attribute maps to the trigger event —
 * that mapping is stored in the retention_policy.trigger_attribute_key column.</p>
 */
public enum RetentionTriggerEvent {

    /**
     * Retention begins from the date the document was ingested into LifecycleCM.
     * Used when no later business event is relevant (e.g., Explanation of Benefits).
     */
    DOCUMENT_CREATION,

    /**
     * Retention begins from the date a medical claim was closed (adjudication complete).
     * Mapped to the claim_close_date attribute.
     */
    CLAIM_CLOSE_DATE,

    /**
     * Retention begins from the date healthcare services were rendered.
     * Mapped to the service_date_from attribute.
     */
    SERVICE_DATE,

    /**
     * Retention begins from the date a member's coverage was terminated.
     * Mapped to the member_termination_date attribute.
     * Used for ERISA enrollment record retention.
     */
    MEMBER_TERMINATION_DATE,

    /**
     * Retention begins from the date an appeal or grievance was resolved.
     * Mapped to the resolution_date attribute.
     */
    RESOLUTION_DATE,

    /**
     * Retention begins from the date a prior authorization decision was rendered.
     * Mapped to the auth_decision_date attribute.
     */
    AUTHORIZATION_DECISION_DATE,

    /**
     * Retention begins from the date a provider contract was terminated.
     * Mapped to the termination_date attribute.
     */
    CONTRACT_TERMINATION_DATE
}
