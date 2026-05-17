package com.ecm.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Immutable DTO representing a single structured audit log entry.
 *
 * <p>Every meaningful business operation in LifecycleCM — document ingestion,
 * lifecycle transition, PHI access, retention engine run, access denial —
 * is logged via AuditLogger using this structure. The structured format ensures
 * log entries are parseable by SIEM tools and audit reporting systems without
 * regex extraction.</p>
 *
 * <p>Fields marked @JsonInclude(NON_NULL) are optional; they are populated when
 * relevant to the operation type and omitted otherwise to keep log volume manageable.</p>
 *
 * <p>Not responsible for: writing to the log output or determining which operations
 * require audit entries — that is owned by AuditLogger in ecm-library-service.</p>
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogEntry {

    // -----------------------------------------------------------------------
    // Required fields — present on every audit log entry

    /** Unique request correlation ID. Propagated from HTTP header or generated at entry point. */
    String correlationId;

    /** ISO-8601 timestamp of the operation. */
    Instant timestamp;

    /** The type of operation being logged (e.g. DOCUMENT_INGESTED, TRANSITION_EXECUTED, PHI_ACCESSED). */
    String operationType;

    /** The service that generated this entry (ecm-library-service or ecm-resource-manager). */
    String serviceId;

    // -----------------------------------------------------------------------
    // Document context — populated for all document-related operations

    /** Internal UUID of the document. */
    String documentId;

    /** Human-readable document number (e.g. DOC-2025-0000441). */
    String documentNumber;

    /** Document class code (e.g. MEDICAL_CLAIM). */
    String documentClassCode;

    // -----------------------------------------------------------------------
    // User context — populated for all human-initiated operations

    /** ID of the user who performed the operation. "SYSTEM" for automated operations. */
    String userId;

    /** Role code of the acting user at the time of the operation. */
    String roleCode;

    /** Client IP address of the request. Null for system-initiated operations. */
    String clientIpAddress;

    // -----------------------------------------------------------------------
    // Lifecycle context — populated for transition-related entries

    /** State before the transition. Null for non-transition operations. */
    String fromState;

    /** State after the transition (or attempted target state for denied transitions). */
    String toState;

    /** Reason a transition was denied. Null for successful transitions. */
    String transitionDenyReason;

    // -----------------------------------------------------------------------
    // Outcome

    /** Whether the operation succeeded. */
    boolean success;

    /** Human-readable description of the outcome or error. */
    String outcomeDetail;
}
