package main.java.com.ecm.library.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a type of document in the repository.
 *
 * <p>A document class is the type contract for a repository document. It specifies
 * which attribute groups apply (and in what display order), whether documents of
 * this class carry PHI, and which retention policy governs them. All of this is
 * data — adding a new document type requires no code change.</p>
 *
 * <p>Not responsible for: holding document instances (those are Document entities),
 * attribute validation logic, or retention schedule computation.</p>
 */
@Entity
@Table(name = "document_class")
@Getter
@Setter
@NoArgsConstructor
public class DocumentClass extends BaseEntity {

    /** Machine-readable type code, e.g. MEDICAL_CLAIM. Unique across all classes. */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "description")
    private String description;

    /**
     * True if documents of this class contain Protected Health Information.
     * PHI-bearing documents trigger automatic phi_access_log entries on every
     * retrieval, regardless of operation type.
     */
    @Column(name = "is_phi_bearing", nullable = false)
    private boolean phiBearing;

    /**
     * False if this class has been retired and should not accept new documents.
     * Existing documents of an inactive class are unaffected.
     */
    @Column(name = "is_active", nullable = false)
    private boolean active;

    /**
     * Code of the governing retention policy, e.g. CMS_MEDICARE_10YR.
     * Stored as a string reference; FK to retention_policy is added in V4.
     */
    @Column(name = "retention_policy_code", length = 50)
    private String retentionPolicyCode;

    /**
     * Ordered list of attribute groups that define this class's metadata schema.
     * display_order controls the sequence in forms and document detail views.
     */
    @OneToMany(
        mappedBy = "documentClass",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<DocumentClassAttributeGroup> attributeGroupMappings = new ArrayList<>();
}
