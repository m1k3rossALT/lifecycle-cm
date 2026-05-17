package com.ecm.common.event;

import com.ecm.common.enums.DocumentClassCode;

/**
 * Fired when a new document is successfully ingested into LifecycleCM and its
 * initial RECEIVED state has been persisted.
 *
 * <p>Primary consumer: WorkflowEventListener, which checks whether the document's
 * class has an associated workflow and creates the first WorkflowTask if so.</p>
 *
 * <p>Secondary consumer (future): notification service, external integration bus.</p>
 */
public interface DocumentReceivedEvent extends DomainEvent {

    /** The Document Class of the newly ingested document. */
    DocumentClassCode getDocumentClassCode();

    /**
     * The ID of the user who submitted the ingestion request.
     * Stored in the first lifecycle_transition record as the initiating user.
     */
    String getSubmittedByUserId();
}
