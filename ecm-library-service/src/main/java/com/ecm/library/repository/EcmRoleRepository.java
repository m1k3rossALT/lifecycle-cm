package main.java.com.ecm.library.repository;

import com.ecm.library.entity.EcmRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EcmRole.
 * findByCode is the primary lookup — roles are always resolved by code at the API boundary.
 */
@Repository
public interface EcmRoleRepository extends JpaRepository<EcmRole, UUID> {

    Optional<EcmRole> findByCode(String code);
}
