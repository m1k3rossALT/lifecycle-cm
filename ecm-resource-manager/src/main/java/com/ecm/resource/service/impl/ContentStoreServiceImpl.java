package main.java.com.ecm.resource.service.impl;

import com.ecm.resource.config.MinioProperties;
import com.ecm.resource.entity.ContentObject;
import com.ecm.resource.exception.ContentNotFoundException;
import com.ecm.resource.repository.ContentObjectRepository;
import com.ecm.resource.service.ContentStoreService;
import com.ecm.resource.service.StoreContentCommand;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.YearMonth;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Implements ContentStoreService using MinIO as the binary store.
 *
 * <p>Storage key format: {document_class_lower}/{year}/{month}/{uuid}.{ext}
 * This creates a time-based hierarchy in MinIO that keeps prefix scans fast
 * and makes manual inspection straightforward.</p>
 *
 * <p>Checksum computation: SHA-256 is computed in a single streaming pass
 * using DigestInputStream — the same bytes uploaded to MinIO are checksummed,
 * with no need to re-read the content after upload.</p>
 *
 * <p>Not responsible for: authorization checks, document lifecycle, or
 * deciding when deletion is permitted — those decisions are made by the
 * library service before calling this service.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentStoreServiceImpl implements ContentStoreService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ContentObjectRepository contentObjectRepository;

    @Override
    @Transactional
    public ContentObject store(StoreContentCommand command) {
        UUID id = UUID.randomUUID();
        String storageKey = buildStorageKey(command.documentClassCode(), id, command.originalFileName());

        try {
            // Wrap the stream with DigestInputStream so checksum is computed
            // in one pass as bytes are streamed to MinIO.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            DigestInputStream digestStream = new DigestInputStream(command.contentStream(), digest);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(storageKey)
                            .stream(digestStream, command.fileSizeBytes(), -1)
                            .contentType(command.contentType())
                            .build()
            );

            String checksum = HexFormat.of().formatHex(digest.digest());

            ContentObject record = new ContentObject();
            record.setStorageKey(storageKey);
            record.setBucketName(minioProperties.getBucketName());
            record.setOriginalFileName(command.originalFileName());
            record.setContentType(command.contentType());
            record.setFileSizeBytes(command.fileSizeBytes());
            record.setSha256Checksum(checksum);
            record.setDocumentClassCode(command.documentClassCode());
            record.setUploadedByUserId(command.uploadedByUserId());

            ContentObject saved = contentObjectRepository.save(record);
            log.info("Stored content object id={} key={} checksum={}", saved.getId(), storageKey, checksum);
            return saved;

        } catch (Exception e) {
            log.error("Failed to store content object key={}", storageKey, e);
            throw new RuntimeException("Content storage failed for key: " + storageKey, e);
        }
    }

    @Override
    public InputStream retrieve(UUID contentObjectId) {
        ContentObject record = contentObjectRepository.findByIdAndDeletedFalse(contentObjectId)
                .orElseThrow(() -> new ContentNotFoundException(contentObjectId));

        try {
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(record.getBucketName())
                            .object(record.getStorageKey())
                            .build()
            );
            log.info("Retrieved content object id={} key={}", contentObjectId, record.getStorageKey());
            return response;

        } catch (Exception e) {
            log.error("Failed to retrieve content object id={} key={}", contentObjectId, record.getStorageKey(), e);
            throw new RuntimeException("Content retrieval failed for id: " + contentObjectId, e);
        }
    }

    @Override
    @Transactional
    public void delete(UUID contentObjectId) {
        ContentObject record = contentObjectRepository.findByIdAndDeletedFalse(contentObjectId)
                .orElseThrow(() -> new ContentNotFoundException(contentObjectId));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(record.getBucketName())
                            .object(record.getStorageKey())
                            .build()
            );

            record.setDeleted(true);
            record.setDeletedAt(Instant.now());
            contentObjectRepository.save(record);
            log.info("Deleted content object id={} key={}", contentObjectId, record.getStorageKey());

        } catch (Exception e) {
            log.error("Failed to delete content object id={} key={}", contentObjectId, record.getStorageKey(), e);
            throw new RuntimeException("Content deletion failed for id: " + contentObjectId, e);
        }
    }

    @Override
    public boolean verifyChecksum(UUID contentObjectId) {
        ContentObject record = contentObjectRepository.findByIdAndDeletedFalse(contentObjectId)
                .orElseThrow(() -> new ContentNotFoundException(contentObjectId));

        try (InputStream stream = retrieve(contentObjectId)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            String recomputed = HexFormat.of().formatHex(digest.digest());
            boolean matches = recomputed.equals(record.getSha256Checksum());

            if (!matches) {
                log.error("Checksum mismatch for content object id={}: recorded={} recomputed={}",
                        contentObjectId, record.getSha256Checksum(), recomputed);
            }
            return matches;

        } catch (Exception e) {
            log.error("Checksum verification failed for content object id={}", contentObjectId, e);
            throw new RuntimeException("Checksum verification failed for id: " + contentObjectId, e);
        }
    }

    // -----------------------------------------------------------------------

    /**
     * Builds the MinIO storage key for a new content object.
     * Format: {class_lower}/{year}/{month:02d}/{uuid}.{ext}
     * Example: medical_claim/2025/05/3f9a1b2c-0000-0000-0000-000000000001.pdf
     */
    private String buildStorageKey(String documentClassCode, UUID id, String originalFileName) {
        YearMonth ym = YearMonth.now();
        String prefix = documentClassCode != null
                ? documentClassCode.toLowerCase()
                : "unknown";
        String extension = extractExtension(originalFileName);
        String suffix = extension.isEmpty() ? id.toString() : id + "." + extension;
        return String.format("%s/%d/%02d/%s", prefix, ym.getYear(), ym.getMonthValue(), suffix);
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        // Guard against path traversal or unusually long extensions
        return (ext.length() <= 10) ? ext.toLowerCase() : "";
    }
}
