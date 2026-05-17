package main.java.com.ecm.library.repository;

import com.ecm.library.entity.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AttributeGroup.
 *
 * <p>findByCode is the primary lookup used by DocumentAttributeService when
 * resolving the attribute group to validate against for a given document class.</p>
 */
@Repository
public interface AttributeGroupRepository extends JpaRepository<AttributeGroup, UUID> {

    Optional<AttributeGroup> findByCode(String code);

    boolean existsByCode(String code);
}
