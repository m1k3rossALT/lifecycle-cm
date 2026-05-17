package main.java.com.ecm.library.repository;

import com.ecm.library.entity.DocumentClassAttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for DocumentClassAttributeGroup.
 *
 * <p>Used by DocumentClassService to load the ordered attribute group
 * assignments for a given document class when building the schema view
 * for the admin console.</p>
 */
@Repository
public interface DocumentClassAttributeGroupRepository
        extends JpaRepository<DocumentClassAttributeGroup, UUID> {

    List<DocumentClassAttributeGroup> findByDocumentClassIdOrderByDisplayOrderAsc(
            UUID documentClassId);
}
