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

/**
 * Join entity linking a DocumentClass to an AttributeGroup, with display ordering.
 *
 * <p>This is not a simple @ManyToMany join table — it carries the display_order
 * column which controls how attribute groups are presented in forms and document
 * detail views. That extra column is why it needs to be a full entity rather than
 * a plain join table annotation.</p>
 *
 * <p>Not responsible for: the attribute definitions within the group, or any
 * validation logic.</p>
 */
@Entity
@Table(name = "document_class_attribute_group")
@Getter
@Setter
@NoArgsConstructor
public class DocumentClassAttributeGroup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_class_id", nullable = false)
    private DocumentClass documentClass;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "attribute_group_id", nullable = false)
    private AttributeGroup attributeGroup;

    /** Position of this group in the document class's schema form. Lower = first. */
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
