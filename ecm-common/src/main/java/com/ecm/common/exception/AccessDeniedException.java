package com.ecm.common.exception;

/**
 * Thrown when a user attempts to perform an operation they are not authorized for.
 *
 * <p>This is the application-level access denial, separate from Spring Security's
 * own AccessDeniedException. It is thrown by AccessControlService when role
 * permission checks fail, and by scope filter logic when a user attempts to access
 * a document outside their permitted scope.</p>
 *
 * <p>Maps to HTTP 403 Forbidden in the global exception handler. The response body
 * includes the errorCode but intentionally omits detailed authorization context to
 * avoid leaking permission structure to callers.</p>
 */
public class AccessDeniedException extends EcmException {

    private static final String ERROR_CODE = "ACCESS_DENIED";

    public AccessDeniedException(String message, String correlationId) {
        super(ERROR_CODE, message, correlationId);
    }
}
