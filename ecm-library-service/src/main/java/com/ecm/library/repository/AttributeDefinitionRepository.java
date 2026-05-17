package main.java.com.ecm.library.repository;

import com.ecm.library.entity.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for AttributeDefinition.
 *
 * <p>findByAttributeGroupId is the primary access pattern — DocumentAttributeService
 * loads all definitions for a group to validate incoming attribute values for a
 * document class before persistence.</p>
 */
@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, UUID> {

    List<AttributeDefinition> findByAttributeGroupId(UUID attributeGroupId);

    /** Finds the definitions flagged as retention triggers within a group. */
    List<AttributeDefinition> findByAttributeGroupIdAndRetentionTriggerTrue(UUID attributeGroupId);
}
