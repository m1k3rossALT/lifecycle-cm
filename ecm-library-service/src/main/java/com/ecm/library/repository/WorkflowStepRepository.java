package main.java.com.ecm.library.repository;

import com.ecm.common.enums.LifecycleState;
import com.ecm.library.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for WorkflowStep.
 *
 * <p>findByWorkflowDefinitionIdAndTriggersOnState is the lookup used by
 * WorkflowEventListener to find which step to activate when a document
 * enters a new lifecycle state.</p>
 */
@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {

    Optional<WorkflowStep> findByWorkflowDefinitionIdAndTriggersOnState(
            UUID workflowDefinitionId, LifecycleState triggersOnState);
}
