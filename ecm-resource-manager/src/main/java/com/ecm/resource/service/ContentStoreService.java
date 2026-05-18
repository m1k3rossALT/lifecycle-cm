package main.java.com.ecm.resource.service;

import com.ecm.resource.entity.ContentObject;

import java.io.InputStream;
import java.util.UUID;

/**
 * Manages binary content storage in MinIO and the corresponding metadata records.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Uploading content to MinIO with SHA-256 checksum computation</li>
 *   <li>Retrieving content streams from MinIO by content object ID</li>
 *   <li>Soft-deleting content after disposition (binary removed, metadata retained)</li>
 *   <li>Verifying content integrity by re-computing and comparing checksums</li>
 * </ul>
 *
 * <p>Not responsible for: authorization, document lifecycle rules, or retention
 * policy — this service does exactly what the library service tells it to do.</p>
 */
public interface ContentStoreService {

    /**
     * Uploads binary content to MinIO, computes its SHA-256 checksum, persists
     * a content_object metadata record, and returns the persisted object.
     *
     * @param command  upload parameters including the content stream
     * @return the persisted ContentObject whose ID becomes the content_reference_id
     *         stored by the library service on the document record
     */
    ContentObject store(StoreContentCommand command);

    /**
     * Opens a stream to retrieve content from MinIO.
     *
     * <p>The caller is responsible for closing the returned stream.</p>
     *
     * @param contentObjectId  ID of the ContentObject to retrieve
     * @return open InputStream of the stored binary content
     * @throws ContentNotFoundException if the ID does not exist or content has been deleted
     */
    InputStream retrieve(UUID contentObjectId);

    /**
     * Deletes the binary content from MinIO and marks the content_object record
     * as deleted. The metadata record is retained permanently.
     *
     * @param contentObjectId  ID of the ContentObject to delete
     * @throws ContentNotFoundException if the ID does not exist or is already deleted
     */
    void delete(UUID contentObjectId);

    /**
     * Re-downloads content from MinIO and recomputes its SHA-256 checksum,
     * comparing it against the value recorded at upload time.
     *
     * @param contentObjectId  ID of the ContentObject to verify
     * @return true if the stored content matches the recorded checksum
     */
    boolean verifyChecksum(UUID contentObjectId);
}
