package main.java.com.ecm.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * An immutable HIPAA PHI access log record.
 *
 * <p>Every access to a PHI-bearing document — search result, detail view,
 * content download — produces one row here. Rows are never updated and
 * never deleted. This log is the primary evidence produced in response
 * to a HIPAA access audit or federal examination.</p>
 *
 * <p>document_number and document_class_code are denormalized onto this
 * record at write time. This ensures audit queries can be answered
 * efficiently without joins, and that the log remains accurate even
 * if hypothetically the document record were modified in future.</p>
 *
 * <p>{@code @Immutable} instructs Hibernate to never issue UPDATE statements
 * for this entity.</p>
 *
 * <p>Not responsible for: deciding when to write a PHI log entry —
 * that is owned by PhiAccessInterceptor (Phase 2) and PhiAccessLogService.</p>
 */
@Entity
@Table(name = "phi_access_log")
@Immutable
@Getter
@Setter
@NoArgsConstructor
public class PhiAccessLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, updatable = false)
    private Document document;

    /** Denormalized for fast audit queries without joins. */
    @Column(name = "document_number", nullable = false, length = 30, updatable = false)
    private String documentNumber;

    /** Denormalized for fast audit queries without joins. */
    @Column(name = "document_class_code", nullable = false, length = 50, updatable = false)
    private String documentClassCode;

    @Column(name = "accessed_by_user_id", nullable = false, length = 100, updatable = false)
    private String accessedByUserId;

    @Column(name = "accessed_by_role", length = 50, updatable = false)
    private String accessedByRole;

    /** VIEW_METADATA | DOWNLOAD_CONTENT | SEARCH_RESULT */
    @Column(name = "access_type", nullable = false, length = 30, updatable = false)
    private String accessType;

    /** Derived from role and operation, e.g. CLAIMS_PROCESSING. */
    @Column(name = "access_reason", length = 100, updatable = false)
    private String accessReason;

    @Column(name = "client_ip_address", length = 45, updatable = false)
    private String clientIpAddress;

    @Column(name = "accessed_at", nullable = false, updatable = false)
    private Instant accessedAt;
}
