package com.ecm.common.enums;

/**
 * The action taken when a document is approved for disposition by a Compliance Officer.
 *
 * <p>Disposition is never automatic. The Retention Engine flags a document as
 * ELIGIBLE_FOR_DISPOSITION; a Compliance Officer must explicitly approve the action.
 * This enum defines what that approved action actually does.</p>
 *
 * <p>In all cases: the document record, attribute values, lifecycle history, and audit
 * trail in ecm-library-service are permanently preserved. Disposition acts only on
 * binary content stored in MinIO via ecm-resource-manager.</p>
 *
 * <p>Not responsible for: executing the action — that is owned by DispositionService.</p>
 */
public enum DispositionAction {

    /**
     * Binary content is deleted from MinIO. The document's content_reference_id
     * is set to null. The document transitions to DISPOSED. Metadata is retained
     * permanently. This is the standard action for most healthcare document classes.
     */
    DELETE,

    /**
     * Binary content is first moved to a lower-cost storage tier within MinIO
     * (or an external archive), then scheduled for eventual deletion after a
     * further hold period. Used for documents where immediate deletion is not
     * operationally practical.
     *
     * <p>Implementation note: the archive tier and secondary hold period are
     * configurable per retention policy. Phase 3 implements the DELETE path;
     * ARCHIVE_THEN_DELETE is reserved for future enhancement.</p>
     */
    ARCHIVE_THEN_DELETE,

    /**
     * Document is paused at the eligibility gate for manual Compliance Officer review
     * even if no formal legal hold exists. Used for document classes where automatic
     * deletion at eligibility is considered too risky without an explicit human sign-off
     * beyond the standard disposition approval flow.
     */
    LEGAL_HOLD_CHECK
}
