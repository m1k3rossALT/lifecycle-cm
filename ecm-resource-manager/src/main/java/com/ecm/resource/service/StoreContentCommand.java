package main.java.com.ecm.resource.service;

import java.io.InputStream;

/**
 * Input command for storing a new content object.
 *
 * <p>A record rather than a class — all fields are set at construction and
 * the object is consumed in a single call to ContentStoreService.store().</p>
 *
 * <p>The caller is responsible for closing the contentStream after the
 * store call returns. ContentStoreServiceImpl does not close it.</p>
 */
public record StoreContentCommand(
        InputStream contentStream,
        String originalFileName,
        String contentType,
        long fileSizeBytes,
        String documentClassCode,
        String uploadedByUserId
) {}
