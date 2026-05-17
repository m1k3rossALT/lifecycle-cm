package com.ecm.common.enums;

/**
 * Enumerates every Document Class configured in the LifecycleCM repository.
 *
 * <p>A Document Class is the type identifier for a repository document. It determines
 * which Attribute Groups apply, whether the document bears PHI, and which retention
 * policy governs it. New document types are added by creating a new enum constant here
 * and a corresponding seed migration — no other code changes required.</p>
 *
 * <p>Not responsible for: attribute definitions, retention policy linkage, or workflow
 * assignment — those are stored as data in the document_class and related tables.</p>
 */
public enum DocumentClassCode {

    /**
     * A formal request by a healthcare provider for reimbursement of services rendered.
     * PHI-bearing. Governed by CMS Medicare 10-year retention.
     */
    MEDICAL_CLAIM("Medical Claim", true),

    /**
     * A request from a provider seeking pre-approval for a specific service,
     * medication, or procedure before it is performed.
     * PHI-bearing. Governed by HIPAA PHI 6-year retention.
     */
    PRIOR_AUTHORIZATION_REQUEST("Prior Authorization Request", true),

    /**
     * A summary statement issued to members explaining how a submitted claim was
     * processed and what portion the health plan paid.
     * PHI-bearing. Governed by HIPAA EOB 6-year retention.
     */
    EXPLANATION_OF_BENEFITS("Explanation of Benefits", true),

    /**
     * The application or change form submitted when a member enrolls in or modifies
     * their coverage.
     * PHI-bearing. Governed by ERISA 6-year retention.
     */
    MEMBER_ENROLLMENT_FORM("Member Enrollment Form", true),

    /**
     * A formal challenge filed by a member or provider contesting a claim decision
     * or coverage denial.
     * PHI-bearing. Governed by CMS Appeal 6-year retention.
     */
    APPEAL_AND_GRIEVANCE("Appeal and Grievance", true),

    /**
     * A contractual agreement between the organization and a healthcare provider
     * or provider group.
     * Not PHI-bearing. Governed by SOX 7-year retention.
     */
    PROVIDER_CONTRACT("Provider Contract", false);

    // -----------------------------------------------------------------------

    /** Display label used in UI and reports. */
    private final String displayName;

    /**
     * Whether documents of this class carry Protected Health Information.
     * PHI-bearing documents trigger automatic phi_access_log entries on retrieval.
     */
    private final boolean phiBearing;

    DocumentClassCode(String displayName, boolean phiBearing) {
        this.displayName = displayName;
        this.phiBearing = phiBearing;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPhiBearing() {
        return phiBearing;
    }
}
