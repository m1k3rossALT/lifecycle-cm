package main.java.com.ecm.library.repository;

import com.ecm.library.entity.DocumentAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for DocumentAttribute.
 *
 * <p>The primary EAV access pattern is: given a document ID, load all attribute
 * rows. For retention trigger resolution, a specific key lookup is needed.
 * Both are covered here.</p>
 */
@Repository
public interface DocumentAttributeRepository extends JpaRepository<DocumentAttribute, UUID> {

    List<DocumentAttribute> findByDocumentId(UUID documentId);

    Optional<DocumentAttribute> findByDocumentIdAndAttributeKey(UUID documentId, String attributeKey);

    boolean existsByDocumentIdAndAttributeKey(UUID documentId, String attributeKey);

    void deleteByDocumentId(UUID documentId);
}
