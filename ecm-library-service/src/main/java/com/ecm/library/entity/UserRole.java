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

import java.time.LocalDate;

/**
 * A role assignment linking a user to a role, with an effective date window.
 *
 * <p>The effective date window allows role assignments to be scheduled in
 * advance (future effective_from) or to expire automatically (effective_to).
 * UserService checks active assignments by comparing CURRENT_DATE against
 * the window.</p>
 *
 * <p>Records are never deleted — to revoke a role, set effective_to to a
 * past date. The full assignment history is preserved for audit purposes.</p>
 *
 * <p>Not responsible for: evaluating whether an assignment is currently
 * active — that check is in UserService.</p>
 */
@Entity
@Table(name = "user_role")
@Getter
@Setter
@NoArgsConstructor
public class UserRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private EcmUser user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private EcmRole role;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    /** Null means no expiry. Set to a past date to revoke without deleting. */
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "assigned_by_user_id", length = 100)
    private String assignedByUserId;
}
