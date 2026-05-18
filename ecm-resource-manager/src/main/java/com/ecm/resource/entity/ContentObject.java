package main.java.com.ecm.resource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Metadata record for a single binary object stored in MinIO.
 *
 * <p>The resource manager knows nothing about document classes, lifecycle states,
 * or retention policies. This entity records only what is needed to address,
 * retrieve, verify, and eventually mark as deleted the corresponding MinIO object.</p>
 *
 * <p>When content is disposed, {@code deleted} is set to true and {@code deletedAt}
 * is recorded. The binary is removed from MinIO. This record is never deleted —
 * it is the proof that the content existed, what its checksum was, and when it
 * was destroyed.</p>
 *
 * <p>Not responsible for: authorization, lifecycle rules, or retention policy —
 * all of that is the library service's domain.</p>
 */
@Entity
@Table(name = "content_object")
@Getter
@Setter
@NoArgsConstructor
public class ContentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * The MinIO object key. Format: {class_lower}/{year}/{month}/{id}.{ext}
     * Immutable after creation — if content is replaced, a new ContentObject is created.
     */
    @Column(name = "storage_key", nullable = false, unique = true, length = 500, updatable = false)
    private String storageKey;

    @Column(name = "bucket_name", nullable = false, length = 100, updatable = false)
    private String bucketName;

    @Column(name = "original_file_name", length = 255, updatable = false)
    private String originalFileName;

    @Column(name = "content_type", length = 100, updatable = false)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false, updatable = false)
    private long fileSizeBytes;

    /** Hex-encoded SHA-256 digest computed during upload. */
    @Column(name = "sha256_checksum", nullable = false, length = 64, updatable = false)
    private String sha256Checksum;

    @Column(name = "document_class_code", length = 50, updatable = false)
    private String documentClassCode;

    @Column(name = "uploaded_by_user_id", length = 100, updatable = false)
    private String uploadedByUserId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;
}
