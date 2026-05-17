package com.ecm.common.event;

import java.time.LocalDate;

/**
 * Fired by the Retention Engine when it determines that a document has satisfied
 * its full retention obligation and has been flagged as ELIGIBLE_FOR_DISPOSITION.
 *
 * <p>Primary consumer: the disposition task creator, which creates a visible entry
 * in the Compliance Officer's disposition approval queue in ecm-admin.</p>
 *
 * <p>This event is fired once per document per eligibility determination. If a
 * legal hold is placed after the event fires, the document transitions to
 * ON_LEGAL_HOLD; the event is not re-fired when the hold is released.
 * The Retention Engine re-evaluates the document on its next scheduled run.</p>
 */
public interface RetentionEligibilityFlaggedEvent extends DomainEvent {

    /** Code of the retention policy that determined eligibility. */
    String getPolicyCode();

    /** The date the retention clock started for this document. */
    LocalDate getTriggerDate();

    /** The date on which the document became eligible (trigger date + retention years). */
    LocalDate getEligibleForDispositionDate();
}
