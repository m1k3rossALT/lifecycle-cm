package main.java.com.ecm.library.entity;

import com.ecm.common.enums.AttributeDataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single typed metadata field definition within an attribute group.
 *
 * <p>Defines the data type, validation constraints, and display ordering for
 * one attribute field. DocumentAttributeService reads these definitions to
 * validate incoming attribute values before persistence.</p>
 *
 * <p>The {@code attributeKey} is the machine-readable field identifier used
 * in document_attribute rows (e.g. "member_id"). It is the link between the
 * schema definition and the EAV data.</p>
 *
 * <p>Not responsible for: holding or validating actual attribute values —
 * those are in DocumentAttribute. Validation logic is in DocumentAttributeService.</p>
 */
@Entity
@Table(name = "attribute_definition")
@Getter
@Setter
@NoArgsConstructor
public class AttributeDefinition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_group_id", nullable = false)
    private AttributeGroup attributeGroup;

    /**
     * Machine-readable field identifier. Unique within its attribute group.
     * Used as the key in document_attribute EAV rows.
     */
    @Column(name = "attribute_key", nullable = false, length = 100)
    private String attributeKey;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** Determines which validation rule applies to incoming values for this field. */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private AttributeDataType dataType;

    /** Whether a value for this field must be present on every document of the class. */
    @Column(name = "required", nullable = false)
    private boolean required;

    /** Maximum character length. Applies to STRING type only. Null for other types. */
    @Column(name = "max_length")
    private Integer maxLength;

    /**
     * Comma-separated list of permitted values. Applies to ENUM type only.
     * Example: "IN_NETWORK,OUT_OF_NETWORK,UNKNOWN"
     */
    @Column(name = "allowed_values")
    private String allowedValues;

    /**
     * True if this field holds a date value that the Retention Engine uses as
     * the start of the retention clock for the governing policy.
     * Example: claim_close_date for CMS_MEDICARE_10YR.
     */
    @Column(name = "is_retention_trigger", nullable = false)
    private boolean retentionTrigger;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
