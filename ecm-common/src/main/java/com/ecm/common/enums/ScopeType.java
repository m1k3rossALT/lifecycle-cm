package com.ecm.common.enums;

/**
 * Controls how much of the document repository a user can see within their
 * permitted Document Classes.
 *
 * <p>Scope filtering is the third layer of the LifecycleCM access control model,
 * applied after authentication and role-based permission checks. Even if a user's
 * role has can_read permission for a Document Class, scope limits which specific
 * documents are returned in search results and visible in their queue.</p>
 *
 * <p>Not responsible for: applying the filter — that is owned by
 * DocumentSearchService in ecm-library-service.</p>
 */
public enum ScopeType {

    /**
     * User sees only documents assigned to their specific processing queue
     * (matched on the assigned_queue attribute). Used for Claims Processors
     * who should only see work routed to their queue.
     */
    OWN_QUEUE,

    /**
     * User sees all documents within their processing region
     * (matched on the processing_region attribute). Used for Regional Managers.
     */
    OWN_REGION,

    /**
     * User sees all documents in the repository for their permitted Document Classes.
     * Used for Compliance Officers, Auditors, Medical Directors, and System Admins.
     */
    ALL
}
