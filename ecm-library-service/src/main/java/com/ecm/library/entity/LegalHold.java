package main.java.com.ecm.library.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A formal instruction to suspend normal retention and disposition
 * for a defined set of documents.
 *
 * <p>Holds are placed and released by Compliance Officers. They are never
 * deleted — releasing a hold sets {@code active = false} and records who
 * released it and when. The full hold history is preserved.</p>
 *
 * <p>A document under an active hold cannot transition to
 * ELIGIBLE_FOR_DISPOSITION. If it is already in that state, it transitions
 * to ON_LEGAL_HOLD. This guard is enforced in the state machine configuration.</p>
 *
 * <p>Not responsible for: enforcing the hold in the state machine —
 * that is owned by the LegalHoldGuard in the statemachine config.</p>
 */
@Entity
@Table(name = "legal_hold")
@Getter
@Setter
@NoArgsConstructor
public class LegalHold extends BaseEntity {

    /** Human-readable hold identifier, e.g. LH-2025-CMS-0041. Unique. */
    @Column(name = "hold_identifier", nullable = false, unique = true, length = 50)
    private String holdIdentifier;

    /** LITIGATION | REGULATORY_INQUIRY | INTERNAL_AUDIT */
    @Column(name = "hold_type", nullable = false, length = 30)
    private String holdType;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "placed_by_user_id", nullable = false, length = 100)
    private String placedByUserId;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Column(name = "released_by_user_id", length = 100)
    private String releasedByUserId;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** The documents covered by this hold. */
    @OneToMany(
        mappedBy = "legalHold",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<LegalHoldDocument> documents = new ArrayList<>();
}
