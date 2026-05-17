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
 * A named, reusable collection of typed attribute definitions.
 *
 * <p>Attribute groups are the reuse mechanism in the document model. A field like
 * member_id is defined once in the MEMBER_ATTRIBUTES group and attached to every
 * document class that needs it. When the validation rule for member_id changes,
 * it changes in one row in attribute_definition, not in five document classes.</p>
 *
 * <p>Not responsible for: determining which document classes use this group —
 * that relationship is owned by DocumentClassAttributeGroup.</p>
 */
@Entity
@Table(name = "attribute_group")
@Getter
@Setter
@NoArgsConstructor
public class AttributeGroup extends BaseEntity {

    /** Machine-readable identifier, e.g. MEMBER_ATTRIBUTES. Unique across all groups. */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "description")
    private String description;

    /**
     * Attribute definitions belonging to this group, ordered by display_order.
     * Loaded eagerly because groups are always needed with their definitions
     * (used together in attribute validation and form rendering).
     */
    @OneToMany(
        mappedBy = "attributeGroup",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
}
