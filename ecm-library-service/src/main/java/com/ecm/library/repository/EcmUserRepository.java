package main.java.com.ecm.library.repository;

import com.ecm.library.entity.EcmUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EcmUser.
 *
 * <p>findByUsernameAndActiveTrue is the login lookup —
 * inactive users must not be able to authenticate.</p>
 */
@Repository
public interface EcmUserRepository extends JpaRepository<EcmUser, UUID> {

    Optional<EcmUser> findByUsername(String username);

    Optional<EcmUser> findByUsernameAndActiveTrue(String username);

    boolean existsByUsername(String username);
}
