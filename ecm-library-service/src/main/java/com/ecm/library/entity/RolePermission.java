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
 * One row in the permission matrix: what a role may do for a specific document class.
 *
 * <p>A missing row means no access. A present row with all flags false would be
 * unusual but is not blocked — it simply grants no permissions.</p>
 *
 * <p>documentClassCode is stored as a plain string rather than a FK to
 * document_class to simplify the AccessControlService lookup by code, which is
 * the natural key at the API boundary.</p>
 *
 * <p>Not responsible for: evaluating permissions or checking scope — that is
 * owned by AccessControlService.</p>
 */
@Entity
@Table(name = "role_permission")
@Getter
@Setter
@NoArgsConstructor
public class RolePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private EcmRole role;

    @Column(name = "document_class_code", nullable = false, length = 50)
    private String documentClassCode;

    @Column(name = "can_read", nullable = false)
    private boolean canRead;

    @Column(name = "can_create", nullable = false)
    private boolean canCreate;

    /** Grants the ability to trigger lifecycle state transitions. */
    @Column(name = "can_transition", nullable = false)
    private boolean canTransition;

    /** Separate flag: disposition is destructive and requires explicit grant. */
    @Column(name = "can_dispose", nullable = false)
    private boolean canDispose;
}
