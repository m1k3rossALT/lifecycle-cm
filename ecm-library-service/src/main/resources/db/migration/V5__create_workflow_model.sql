-- ============================================================
-- V5 — Workflow Engine
-- Creates: workflow_definition, workflow_step, workflow_task
--
-- The workflow model is state-driven: document lifecycle state
-- transitions create and resolve work items. When a document
-- enters a state that has a workflow step defined for it,
-- a task is created in the responsible role queue.
-- ============================================================

-- ------------------------------------------------------------
-- workflow_definition
-- A named workflow process that can be associated with a
-- document class. The Prior Authorization Processing workflow
-- is seeded in V14.
-- ------------------------------------------------------------
CREATE TABLE workflow_definition (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    code            VARCHAR(50) NOT NULL,
    display_name    VARCHAR(150) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_workflow_definition PRIMARY KEY (id),
    CONSTRAINT uq_workflow_definition_code UNIQUE (code)
);

-- ------------------------------------------------------------
-- workflow_step
-- A single step in a workflow. Defines which lifecycle state
-- triggers this step, which role queue receives the task,
-- and what state the document transitions to on completion.
-- ------------------------------------------------------------
CREATE TABLE workflow_step (
    id                      UUID        NOT NULL DEFAULT gen_random_uuid(),
    workflow_definition_id  UUID        NOT NULL,
    step_number             INTEGER     NOT NULL,
    display_name            VARCHAR(150) NOT NULL,
    -- The lifecycle state that triggers creation of this step's task
    triggers_on_state       VARCHAR(30) NOT NULL,
    -- Role queue that receives the task (matches RoleCode enum)
    assigned_role_code      VARCHAR(50) NOT NULL,
    -- SLA in hours from task creation to due_at
    sla_hours               INTEGER     NOT NULL,
    -- For urgent documents: sla_hours * urgency_multiplier
    -- e.g. 0.25 means urgent SLA is 25% of standard
    urgency_sla_multiplier  DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    -- The state to transition the document to when this step is completed
    resolves_to_state       VARCHAR(30) NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_workflow_step PRIMARY KEY (id),
    CONSTRAINT fk_ws_workflow FOREIGN KEY (workflow_definition_id)
        REFERENCES workflow_definition (id),
    CONSTRAINT uq_workflow_step_number UNIQUE (workflow_definition_id, step_number)
);

COMMENT ON COLUMN workflow_step.urgency_sla_multiplier IS 'Multiplier applied to sla_hours for URGENT documents. 0.25 = quarter of standard SLA.';

-- ------------------------------------------------------------
-- workflow_task
-- An individual work item assigned to a role queue for a
-- specific document. Created by WorkflowEventListener on
-- lifecycle state transitions that match a workflow step.
-- ------------------------------------------------------------
CREATE TABLE workflow_task (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    document_id         UUID        NOT NULL,
    workflow_step_id    UUID        NOT NULL,
    -- status matches WorkflowTaskStatus enum
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- Priority: higher = more urgent. Computed from SLA remaining + urgency_level attribute.
    priority            INTEGER     NOT NULL DEFAULT 0,
    assigned_role_code  VARCHAR(50) NOT NULL,
    -- Set when a user claims the task from the queue
    claimed_by_user_id  VARCHAR(100),
    claimed_at          TIMESTAMP WITH TIME ZONE,
    due_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_by_user_id VARCHAR(100),
    completed_at        TIMESTAMP WITH TIME ZONE,
    -- Escalation tracking
    escalated_at        TIMESTAMP WITH TIME ZONE,
    escalation_reason   TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_workflow_task PRIMARY KEY (id),
    CONSTRAINT fk_wt_document FOREIGN KEY (document_id) REFERENCES document (id),
    CONSTRAINT fk_wt_step FOREIGN KEY (workflow_step_id) REFERENCES workflow_step (id)
);

COMMENT ON COLUMN workflow_task.priority IS 'Computed: higher number = shown first in queue. Recalculated by SLA scanner.';
COMMENT ON COLUMN workflow_task.claimed_by_user_id IS 'Set when task is claimed. Cleared if task is released back to queue.';

CREATE INDEX idx_wt_document_id         ON workflow_task (document_id);
CREATE INDEX idx_wt_status_role         ON workflow_task (status, assigned_role_code);
CREATE INDEX idx_wt_claimed_user        ON workflow_task (claimed_by_user_id) WHERE claimed_by_user_id IS NOT NULL;
CREATE INDEX idx_wt_due_at              ON workflow_task (due_at) WHERE status NOT IN ('COMPLETED', 'CANCELLED');
