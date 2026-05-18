package main.java.com.ecm.resource.exception;

import java.util.UUID;

/**
 * Thrown when a requested content object does not exist or has been deleted.
 * Maps to HTTP 404 in the global exception handler.
 */
public class ContentNotFoundException extends RuntimeException {

    private final UUID contentObjectId;

    public ContentNotFoundException(UUID contentObjectId) {
        super("Content object not found or deleted: " + contentObjectId);
        this.contentObjectId = contentObjectId;
    }

    public UUID getContentObjectId() {
        return contentObjectId;
    }
}
