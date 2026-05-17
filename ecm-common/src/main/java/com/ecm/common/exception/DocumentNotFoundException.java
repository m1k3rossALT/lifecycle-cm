package com.ecm.common.exception;

/**
 * Thrown when a requested document cannot be found by document number or ID.
 *
 * <p>Maps to HTTP 404 Not Found in the global exception handler.
 * The document number (or ID) is embedded in the message for log traceability.</p>
 */
public class DocumentNotFoundException extends EcmException {

    private static final String ERROR_CODE = "DOCUMENT_NOT_FOUND";

    public DocumentNotFoundException(String documentNumber, String correlationId) {
        super(ERROR_CODE,
              "Document not found: " + documentNumber,
              correlationId);
    }
}
