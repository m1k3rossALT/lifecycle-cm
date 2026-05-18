package main.java.com.ecm.resource.repository;

import com.ecm.resource.entity.ContentObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ContentObject.
 *
 * <p>findByIdAndDeletedFalse is the primary retrieval lookup — deleted content
 * objects exist as metadata records but their binaries are gone from MinIO.
 * Attempting to download a deleted object must fail clearly, not silently.</p>
 */
@Repository
public interface ContentObjectRepository extends JpaRepository<ContentObject, UUID> {

    Optional<ContentObject> findByIdAndDeletedFalse(UUID id);
}
