package main.java.com.ecm.library.repository;

import com.ecm.library.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RolePermission.
 *
 * <p>findByRoleIdAndDocumentClassCode is the inner-loop lookup in
 * AccessControlService — called on every document operation to check
 * whether the requesting role has the required permission.</p>
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    Optional<RolePermission> findByRoleIdAndDocumentClassCode(UUID roleId, String documentClassCode);

    List<RolePermission> findByRoleId(UUID roleId);
}
