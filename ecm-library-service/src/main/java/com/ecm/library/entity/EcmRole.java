package main.java.com.ecm.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A system-defined role governing access to document classes and operations.
 *
 * <p>Roles are seeded at startup and are not created by end users.
 * The code maps to the {@link com.ecm.common.enums.RoleCode} enum.
 * Permission details per document class are in RolePermission.</p>
 *
 * <p>Not responsible for: permission evaluation or user assignment —
 * those are owned by AccessControlService and UserService respectively.</p>
 */
@Entity
@Table(name = "ecm_role")
@Getter
@Setter
@NoArgsConstructor
public class EcmRole extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "description")
    private String description;
}
