package com.ecm.common.enums;

/**
 * All defined roles in the LifecycleCM access control model.
 *
 * <p>Every authenticated user has one or more roles. Role membership determines:
 * which Document Classes the user can access, which lifecycle transitions they may
 * trigger, and which administrative functions are available to them.</p>
 *
 * <p>Roles are not mutually exclusive by design, but in practice processing staff
 * hold exactly one role. SYSTEM_ADMIN is never used for document processing work.</p>
 *
 * <p>Not responsible for: the specific permission matrix (can_read, can_create, etc.)
 * — that is stored in the role_permission table and enforced by AccessControlService.</p>
 */
public enum RoleCode {

    /**
     * Processes incoming claims and prior authorization requests.
     * Works from an assigned task queue.
     * Default scope: OWN_QUEUE.
     */
    CLAIMS_PROCESSOR,

    /**
     * Performs clinical review of prior authorization requests and complex claims.
     * Has elevated access to clinical document classes.
     * Default scope: ALL within clinical document classes.
     */
    MEDICAL_DIRECTOR,

    /**
     * Assists members with enrollment queries, EOB questions, and appeal submissions.
     * Default scope: member-scoped (documents for assigned members only).
     */
    MEMBER_SERVICES_REPRESENTATIVE,

    /**
     * Manages provider contracts and resolves provider-facing disputes.
     * Default scope: provider-scoped.
     */
    PROVIDER_RELATIONS,

    /**
     * Full read access across all document classes. Manages legal holds, approves
     * dispositions, reviews PHI access logs.
     * Default scope: ALL.
     */
    COMPLIANCE_OFFICER,

    /**
     * Read-only access across all document classes for audit and reporting.
     * Cannot transition documents, place holds, or take any write action.
     * Default scope: ALL.
     */
    AUDITOR,

    /**
     * Full system access including user management, document class configuration,
     * and retention policy administration.
     * Not a processing role — must not be used for document work.
     * Default scope: ALL.
     */
    SYSTEM_ADMIN
}
