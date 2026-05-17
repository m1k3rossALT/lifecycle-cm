package main.java.com.ecm.library.entity;

import com.ecm.common.enums.ScopeType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

/**
 * An authenticated user principal in LifecycleCM.
 *
 * <p>Users are never deleted — deactivating sets {@code active = false}
 * and preserves the record and audit trail. The password is stored as a
 * bcrypt hash; plaintext passwords are never persisted.</p>
 *
 * <p>Not responsible for: password verification or JWT generation —
 * those are owned by AuthService. Permission evaluation is owned by
 * AccessControlService.</p>
 */
@Entity
@Table(name = "ecm_user")
@Getter
@Setter
@NoArgsConstructor
public class EcmUser extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /** Bcrypt hash. Never the plaintext password. */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", length = 255)
    private String email;

    /**
     * Controls how much of the repository this user can see within
     * their permitted document classes.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private ScopeType scopeType = ScopeType.OWN_QUEUE;

    /** Used when scopeType = OWN_REGION. Null otherwise. */
    @Column(name = "region_code", length = 20)
    private String regionCode;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Active and historical role assignments for this user. */
    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    private List<UserRole> userRoles = new ArrayList<>();

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
