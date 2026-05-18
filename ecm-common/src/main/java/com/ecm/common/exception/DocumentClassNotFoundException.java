package com.ecm.common.exception;

/**
 * Thrown when a requested document class code does not exist or is inactive.
 *
 * <p>Maps to HTTP 404 Not Found in the global exception handler.</p>
 */
public class DocumentClassNotFoundException extends EcmException {

    private static final String ERROR_CODE = "DOCUMENT_CLASS_NOT_FOUND";

    public DocumentClassNotFoundException(String classCode, String correlationId) {
        super(ERROR_CODE,
              "Document class not found or inactive: " + classCode,
              correlationId);
    }
}
