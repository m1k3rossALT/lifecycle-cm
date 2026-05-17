package main.java.com.ecm.library.repository;

import com.ecm.library.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for UserRole.
 * findActiveRolesForUser is the primary access pattern used by the JWT auth filter
 * to populate the SecurityContext with a user's currently active roles.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    /**
     * Returns role assignments that are currently active for a user —
     * effective_from is on or before today and effective_to is null or in the future.
     */
    @Query("""
        SELECT ur FROM UserRole ur
        WHERE ur.user.id = :userId
          AND ur.effectiveFrom <= :today
          AND (ur.effectiveTo IS NULL OR ur.effectiveTo >= :today)
        """)
    List<UserRole> findActiveRolesForUser(@Param("userId") UUID userId,
                                          @Param("today") LocalDate today);
}
