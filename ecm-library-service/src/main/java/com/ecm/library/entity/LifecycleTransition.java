package main.java.com.ecm.library.entity;

import com.ecm.common.enums.LifecycleState;
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
import org.hibernate.annotations.Immutable;

/**
 * An immutable audit record of a single lifecycle state transition.
 *
 * <p>Every state change — without exception — produces one row in this table.
 * Rows are never updated and never deleted. This is the primary evidence produced
 * in response to a compliance audit or federal examination.</p>
 *
 * <p>{@code @Immutable} instructs Hibernate to never issue UPDATE statements for
 * this entity. Any attempt to modify a persisted LifecycleTransition and call
 * save() will be silently ignored by Hibernate, not cause an error — which is
 * the correct behaviour for an append-only audit log.</p>
 *
 * <p>fromState is null only for the initial RECEIVED transition, when there is
 * no prior state to record.</p>
 *
 * <p>initiatedByUserId will be the literal string "SYSTEM" for transitions
 * triggered by automated processes (Retention Engine, workflow engine).</p>
 *
 * <p>Not responsible for: executing transitions or enforcing guards — those
 * are LifecycleService's responsibilities.</p>
 */
@Entity
@Table(name = "lifecycle_transition")
@Immutable
@Getter
@Setter
@NoArgsConstructor
public class LifecycleTransition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, updatable = false)
    private Document document;

    /** State before this transition. Null for the initial RECEIVED ingestion record. */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", length = 30, updatable = false)
    private LifecycleState fromState;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false, length = 30, updatable = false)
    private LifecycleState toState;

    /**
     * User ID of the person or system that triggered this transition.
     * "SYSTEM" for Retention Engine and other automated processes.
     */
    @Column(name = "initiated_by_user_id", nullable = false, length = 100, updatable = false)
    private String initiatedByUserId;

    /** Role code of the initiating user. Null for system-initiated transitions. */
    @Column(name = "initiated_by_role", length = 50, updatable = false)
    private String initiatedByRole;

    /** Optional note recorded by the user at the time of the transition. */
    @Column(name = "transition_note", updatable = false)
    private String transitionNote;

    /**
     * Client IP address of the request. Captured for HIPAA traceability on
     * PHI-bearing document transitions. Null for system-initiated transitions.
     */
    @Column(name = "client_ip_address", length = 45, updatable = false)
    private String clientIpAddress;
}
