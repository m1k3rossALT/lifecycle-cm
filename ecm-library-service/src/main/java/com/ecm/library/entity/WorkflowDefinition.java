package main.java.com.ecm.library.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A named workflow process associated with a document class.
 *
 * <p>The workflow definition owns an ordered list of steps. WorkflowEventListener
 * looks up the definition for a document's class and finds which step
 * triggers on the current lifecycle state.</p>
 *
 * <p>Not responsible for: creating tasks or evaluating SLAs — those are
 * owned by WorkflowService and the SLA breach scheduler.</p>
 */
@Entity
@Table(name = "workflow_definition")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowDefinition extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(
        mappedBy = "workflowDefinition",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @OrderBy("stepNumber ASC")
    private List<WorkflowStep> steps = new ArrayList<>();
}
