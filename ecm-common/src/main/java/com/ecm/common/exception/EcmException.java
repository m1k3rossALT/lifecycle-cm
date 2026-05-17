package com.ecm.common.exception;

/**
 * Base exception for all application-level errors in LifecycleCM.
 *
 * <p>All domain exceptions extend this class. This makes it possible for
 * exception handlers in each service to catch EcmException as a single type
 * and extract correlationId and errorCode for structured error responses, while
 * still distinguishing between specific error types where needed.</p>
 *
 * <p>Every exception carries:
 * <ul>
 *   <li>{@code errorCode} — a machine-readable code for the specific error condition,
 *       used in API responses and log entries (e.g. "DOCUMENT_NOT_FOUND")</li>
 *   <li>{@code correlationId} — the request correlation ID, propagated from the
 *       MDC by AuditLogger. Allows a single log line to be traced across services.</li>
 * </ul>
 * </p>
 *
 * <p>Not responsible for: HTTP status mapping — that is done by the global exception
 * handler (@ControllerAdvice) in each service module.</p>
 */
public class EcmException extends RuntimeException {

    /** Machine-readable code identifying the error condition. Never null. */
    private final String errorCode;

    /**
     * Request correlation ID at the time the exception was created.
     * May be null if the exception is constructed outside of a request context
     * (e.g. in a scheduled job), in which case the caller should log job run ID instead.
     */
    private final String correlationId;

    public EcmException(String errorCode, String message, String correlationId) {
        super(message);
        this.errorCode = errorCode;
        this.correlationId = correlationId;
    }

    public EcmException(String errorCode, String message, String correlationId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.correlationId = correlationId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
