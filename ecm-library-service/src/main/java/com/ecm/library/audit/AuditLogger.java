package main.java.com.ecm.library.audit;

import com.ecm.common.dto.AuditLogEntry;
import com.ecm.common.enums.LifecycleState;
import com.ecm.common.exception.TransitionDeniedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Writes structured audit log entries for all significant business operations.
 *
 * <p>Every method builds an {@link AuditLogEntry}, serializes it to JSON, and
 * writes it through SLF4J at INFO level. The log output is a stream of JSON
 * objects — one per line — suitable for ingestion by SIEM tools, log aggregators,
 * and compliance reporting pipelines without regex extraction.</p>
 *
 * <p>The correlationId is read from MDC at the time of each log call. It is set
 * by the JWT auth filter for HTTP requests and must be set by scheduled jobs
 * (Retention Engine, SLA scanner) at the start of each job run.</p>
 *
 * <p>Not responsible for: setting the MDC correlationId, PHI access logging
 * (that is PhiAccessLogService), or persisting audit records to the database —
 * this class writes to the application log only. lifecycle_transition records
 * are written separately by LifecycleService.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogger {

    // Using a dedicated logger name so audit entries can be routed to a
    // separate log file or log sink via logback configuration.
    private static final org.slf4j.Logger AUDIT_LOG =
            org.slf4j.LoggerFactory.getLogger("ECM_AUDIT");

    private final ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // Document operations

    public void logDocumentIngested(String documentId, String documentNumber,
                                    String documentClassCode, String userId,
                                    String roleCode, String clientIp) {
        write(AuditLogEntry.builder()
                .correlationId(correlationId())
                .timestamp(Instant.now())
                .operationType("DOCUMENT_INGESTED")
                .serviceId("ecm-library-service")
                .documentId(documentId)
                .documentNumber(documentNumber)
                .documentClassCode(documentClassCode)
                .userId(userId)
                .roleCode(roleCode)
                .clientIpAddress(clientIp)
                .success(true)
                .outcomeDetail("Document ingested and assigned to RECEIVED state")
                .build());
    }

    public void logDocumentRetrieved(String documentId, String documentNumber,
                                     String documentClassCode, String userId,
                                     String roleCode) {
        write(AuditLogEntry.builder()
                .correlationId(correlationId())
                .timestamp(Instant.now())
                .operationType("DOCUMENT_RETRIEVED")
                .serviceId("ecm-library-service")
                .documentId(documentId)
                .documentNumber(documentNumber)
                .documentClassCode(documentClassCode)
                .userId(userId)
                .roleCode(roleCode)
                .success(true)
                .build());
    }

    // -----------------------------------------------------------------------
    // Lifecycle operations

    public void logTransitionExecuted(String documentId, String documentNumber,
                                      String documentClassCode,
                                      LifecycleState fromState, LifecycleState toState,
                                      String userId, String roleCode, String clientIp) {
        write(AuditLogEntry.builder()
                .correlationId(correlationId())
                .timestamp(Instant.now())
                .operationType("LIFECYCLE_TRANSITION_EXECUTED")
                .serviceId("ecm-library-service")
                .documentId(documentId)
                .documentNumber(documentNumber)
                .documentClassCode(documentClassCode)
                .userId(userId)
                .roleCode(roleCode)
                .clientIpAddress(clientIp)
                .fromState(fromState != null ? fromState.name() : null)
                .toState(toState.name())
                .success(true)
                .build());
    }

    public void logTransitionDenied(String documentId, String documentNumber,
                                    String documentClassCode,
                                    LifecycleState fromState, LifecycleState toState,
                                    TransitionDeniedException.DenyReason reason,
                                    String userId, String roleCode, String clientIp) {
        write(AuditLogEntry.builder()
                .correlationId(correlationId())
                .timestamp(Instant.now())
                .operationType("LIFECYCLE_TRANSITION_DENIED")
                .serviceId("ecm-library-service")
                .documentId(documentId)
                .documentNumber(documentNumber)
                .documentClassCode(documentClassCode)
                .userId(userId)
                .roleCode(roleCode)
                .clientIpAddress(clientIp)
                .fromState(fromState != null ? fromState.name() : null)
                .toState(toState.name())
                .transitionDenyReason(reason.name())
                .success(false)
                .outcomeDetail("Transition denied: " + reason.name())
                .build());
    }

    // -----------------------------------------------------------------------
    // Access control

    public void logAccessDenied(String documentId, String documentClassCode,
                                String operation, String userId, String roleCode) {
        write(AuditLogEntry.builder()
                .correlationId(correlationId())
                .timestamp(Instant.now())
                .operationType("ACCESS_DENIED")
                .serviceId("ecm-library-service")
                .documentId(documentId)
                .documentClassCode(documentClassCode)
                .userId(userId)
                .roleCode(roleCode)
                .success(false)
                .outcomeDetail("Access denied for operation: " + operation)
                .build());
    }

    // -----------------------------------------------------------------------
    // Retention operations

    public void logRetentionScheduleComputed(String documentId, String documentNumber,
                                             String policyCode, String eligibleDate,
                                             String jobRunId) {
        write(AuditLogEntry.builder()
                .correlationId(jobRunId)
                .timestamp(Instant.now())
                .operationType("RETENTION_SCHEDULE_COMPUTED")
                .serviceId("ecm-library-service")
                .documentId(documentId)
                .documentNumber(documentNumber)
                .userId("SYSTEM")
                .success(true)
                .outcomeDetail("Policy=" + policyCode + " eligible=" + eligibleDate)
                .build());
    }

    public void logDocumentFlaggedEligible(String documentId, String documentNumber,
                                           String policyCode, String jobRunId) {
        write(AuditLogEntry.builder()
                .correlationId(jobRunId)
                .timestamp(Instant.now())
                .operationType("DOCUMENT_FLAGGED_ELIGIBLE")
                .serviceId("ecm-library-service")
                .documentId(documentId)
                .documentNumber(documentNumber)
                .userId("SYSTEM")
                .success(true)
                .outcomeDetail("Flagged eligible by retention engine, policy=" + policyCode)
                .build());
    }

    public void logDocumentDisposed(String documentId, String documentNumber,
                                    String userId, String roleCode) {
        write(AuditLogEntry.builder()
                .correlationId(correlationId())
                .timestamp(Instant.now())
                .operationType("DOCUMENT_DISPOSED")
                .serviceId("ecm-library-service")
                .documentId(documentId)
                .documentNumber(documentNumber)
                .userId(userId)
                .roleCode(roleCode)
                .toState(LifecycleState.DISPOSED.name())
                .success(true)
                .outcomeDetail("Content deleted from MinIO; metadata retained")
                .build());
    }

    // -----------------------------------------------------------------------

    private void write(AuditLogEntry entry) {
        try {
            AUDIT_LOG.info(objectMapper.writeValueAsString(entry));
        } catch (Exception e) {
            // Audit logging must never crash the calling operation.
            // Log the failure at error level and continue.
            log.error("Failed to serialize audit log entry operationType={}",
                    entry.getOperationType(), e);
        }
    }

    private String correlationId() {
        return MDC.get("correlationId");
    }
}
