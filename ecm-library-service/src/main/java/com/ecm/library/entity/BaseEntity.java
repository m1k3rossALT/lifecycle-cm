package main.java.com.ecm.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Common fields shared by all entities: primary key and creation timestamp.
 *
 * <p>Every table in LifecycleCM has an immutable UUID primary key and an
 * immutable created_at timestamp. Declaring them here avoids repeating the
 * same annotations on every entity class.</p>
 *
 * <p>updatedAt is not here because most entities are insert-only (lifecycle
 * transitions, audit logs). Only Document and DocumentAttribute need it;
 * those entities declare it directly.</p>
 *
 * <p>Not responsible for: validation, business logic, or any field beyond
 * id and createdAt.</p>
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;
}
