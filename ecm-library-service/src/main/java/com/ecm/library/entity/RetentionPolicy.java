package main.java.com.ecm.library.entity;

import com.ecm.common.enums.DispositionAction;
import com.ecm.common.enums.RetentionTriggerEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Defines the retention rules governing a document class.
 *
 * <p>Each policy defines the regulatory basis, what business event starts the
 * retention clock, how many years must pass, and what happens at eligibility.</p>
 *
 * <p>Not responsible for: computing schedules for individual documents or
 * executing disposition — those are RetentionScheduleService and
 * DispositionService respectively.</p>
 */
@Entity
@Table(name = "retention_policy")
@Getter
@Setter
@NoArgsConstructor
public class RetentionPolicy extends BaseEntity {

    @Column(name = "policy_code", nullable = false, unique = true, length = 50)
    private String policyCode;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "regulatory_basis", nullable = false)
    private String regulatoryBasis;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_event", nullable = false, length = 50)
    private RetentionTriggerEvent triggerEvent;

    /**
     * The attribute_key whose value the Retention Engine reads to get the
     * clock start date. e.g. "claim_close_date" for CMS_MEDICARE_10YR.
     */
    @Column(name = "trigger_attribute_key", nullable = false, length = 100)
    private String triggerAttributeKey;

    @Column(name = "retention_years", nullable = false)
    private int retentionYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition_action", nullable = false, length = 30)
    private DispositionAction dispositionAction;
}
