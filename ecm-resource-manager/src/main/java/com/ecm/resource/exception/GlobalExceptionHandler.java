package main.java.com.ecm.resource.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Translates exceptions thrown by service classes into HTTP responses.
 *
 * <p>Error responses use a consistent structure: timestamp, status, errorCode,
 * and message. This makes it straightforward for the library service (the
 * only caller) to distinguish error types programmatically.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleContentNotFound(ContentNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("Unhandled exception in resource manager", e);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> errorResponse(
            HttpStatus status, String errorCode, String message) {

        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status",    status.value(),
                "errorCode", errorCode,
                "message",   message
        ));
    }
}
