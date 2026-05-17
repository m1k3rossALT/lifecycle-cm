package main.java.com.ecm.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Join entity linking a LegalHold to a specific document it covers.
 *
 * <p>A hold can cover many documents; a document can be under multiple
 * simultaneous holds. LegalHoldService checks for any active hold against
 * a document before permitting disposition transitions.</p>
 *
 * <p>Not responsible for: enforcing the hold or transitioning document state
 * — those are owned by LegalHoldService and the state machine guard.</p>
 */
@Entity
@Table(name = "legal_hold_document")
@Getter
@Setter
@NoArgsConstructor
public class LegalHoldDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "legal_hold_id", nullable = false)
    private LegalHold legalHold;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}
