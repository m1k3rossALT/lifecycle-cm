package main.java.com.ecm.library.entity;

import com.ecm.common.enums.LifecycleState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The core repository record for a single document instance.
 *
 * <p>A Document is an instance of a DocumentClass. It owns the lifecycle state,
 * a reference to binary content in ecm-resource-manager, and the versioning chain.
 * All metadata field values are stored in DocumentAttribute (EAV) rows; this
 * entity holds only structural fields that the repository queries on directly.</p>
 *
 * <p>content_reference_id is a UUID pointing to content_object.id in the resource
 * manager's database. It is intentionally not a JPA relationship — it crosses a
 * service boundary. It becomes null after disposition (content deleted).</p>
 *
 * <p>Not responsible for: attribute value storage (DocumentAttribute), lifecycle
 * transition history (LifecycleTransition), or transition rule enforcement
 * (LifecycleService + state machine).</p>
 */
@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
public class Document extends BaseEntity {

    /**
     * Human-readable document identifier, e.g. DOC-2025-0000441.
     * Assigned by DocumentService using the document_number_seq sequence.
     * Immutable after creation.
     */
    @Column(name = "document_number", nullable = false, unique = true, length = 30, updatable = false)
    private String documentNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_class_id", nullable = false, updatable = false)
    private DocumentClass documentClass;

    /**
     * Current position in the document lifecycle state machine.
     * Updated by LifecycleService on every approved transition.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_state", nullable = false, length = 30)
    private LifecycleState lifecycleState;

    /**
     * UUID of the content_object record in ecm-resource-manager.
     * Not a FK — crosses service boundary. Set on ingestion, nulled on disposition.
     */
    @Column(name = "content_reference_id")
    private UUID contentReferenceId;

    /**
     * Version label: 1.0 for initial ingestion, 1.x for metadata corrections,
     * 2.0 for full content replacement.
     */
    @Column(name = "version_label", nullable = false, length = 10)
    private String versionLabel = "1.0";

    /**
     * False when this document has been superseded by a newer version.
     * Only the current version appears in default search results.
     */
    @Column(name = "is_current_version", nullable = false)
    private boolean currentVersion = true;

    /**
     * ID of the document this version supersedes. Self-referencing.
     * Null for initial ingestion (no prior version).
     */
    @Column(name = "previous_version_id")
    private UUID previousVersionId;

    @Column(name = "created_by_user_id", length = 100)
    private String createdByUserId;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Attribute values for this document, keyed by attribute_key. */
    @OneToMany(
        mappedBy = "document",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<DocumentAttribute> attributes = new ArrayList<>();

    /** Full immutable history of all state transitions for this document. */
    @OneToMany(
        mappedBy = "document",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    private List<LifecycleTransition> transitions = new ArrayList<>();

    /** Keep updated_at current on every state change or metadata correction. */
    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
