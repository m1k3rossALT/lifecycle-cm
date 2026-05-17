package main.java.com.ecm.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A single metadata field value for a document — one row in the EAV table.
 *
 * <p>All attribute values are stored as TEXT. Type coercion (parsing a DATE
 * value, validating an ENUM value) is handled by DocumentAttributeService
 * before persistence. Storing as TEXT keeps the schema stable regardless of
 * how many attribute types are added in future phases.</p>
 *
 * <p>The attributeKey must match an attribute_definition.attribute_key that
 * belongs to one of the attribute groups assigned to the document's class.
 * That constraint is enforced in DocumentAttributeService, not at the DB level,
 * because enforcing it at the DB level would require a complex multi-table check
 * constraint.</p>
 *
 * <p>Not responsible for: type validation, retention trigger resolution, or
 * determining which attributes are required — those are DocumentAttributeService's
 * responsibilities.</p>
 */
@Entity
@Table(name = "document_attribute")
@Getter
@Setter
@NoArgsConstructor
public class DocumentAttribute extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /**
     * The field identifier matching attribute_definition.attribute_key.
     * Example: "member_id", "claim_close_date", "urgency_level".
     */
    @Column(name = "attribute_key", nullable = false, length = 100)
    private String attributeKey;

    /** Stored as TEXT regardless of the attribute's data type. */
    @Column(name = "attribute_value", nullable = false)
    private String attributeValue;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
