package main.java.com.ecm.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * The computed retention schedule for a single archived document.
 *
 * <p>Created by RetentionScheduleService when a document transitions to ARCHIVED.
 * One record per document — multi-policy conflicts are resolved before writing
 * (most restrictive wins, i.e. latest eligible_for_disposition_date).</p>
 *
 * <p>Status values: SCHEDULED → ELIGIBLE (or ON_LEGAL_HOLD) → DISPOSED.</p>
 *
 * <p>Not responsible for: computing the dates or executing disposition —
 * those are RetentionScheduleService and DispositionService respectively.</p>
 */
@Entity
@Table(name = "retention_schedule")
@Getter
@Setter
@NoArgsConstructor
public class RetentionSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "policy_code", nullable = false, length = 50)
    private String policyCode;

    @Column(name = "trigger_attribute_key", nullable = false, length = 100)
    private String triggerAttributeKey;

    /** The actual date value resolved from the document's trigger attribute. */
    @Column(name = "trigger_date", nullable = false)
    private LocalDate triggerDate;

    /** trigger_date + retention_years. The date on which the document becomes eligible. */
    @Column(name = "eligible_for_disposition_date", nullable = false)
    private LocalDate eligibleForDispositionDate;

    /** SCHEDULED | ELIGIBLE | ON_LEGAL_HOLD | DISPOSED */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "SCHEDULED";

    @CreationTimestamp
    @Column(name = "computed_at", updatable = false, nullable = false)
    private Instant computedAt;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
