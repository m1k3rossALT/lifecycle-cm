-- ============================================================
-- V14 — Seed: Prior Authorization Processing Workflow
-- Seeds the workflow definition and its 4 steps exactly as
-- specified in Redbook Chapter 7.
-- ============================================================

-- Workflow definition
INSERT INTO workflow_definition (id, code, display_name, description, is_active)
VALUES (
    '00000000-0000-0000-0000-000000000501',
    'PRIOR_AUTH_PROCESSING',
    'Prior Authorization Processing',
    'End-to-end processing workflow for prior authorization requests, from receipt through clinical review to filing.',
    TRUE
);

-- Step 1: Index Authorization Request
-- Triggered when document enters RECEIVED. Assigned to CLAIMS_PROCESSOR.
-- SLA: 4 hours standard. Resolves to INDEXED.
INSERT INTO workflow_step
    (id, workflow_definition_id, step_number, display_name, triggers_on_state,
     assigned_role_code, sla_hours, urgency_sla_multiplier, resolves_to_state)
VALUES (
    '00000000-0000-0000-0000-000000000511',
    '00000000-0000-0000-0000-000000000501',
    1,
    'Index Authorization Request',
    'RECEIVED',
    'CLAIMS_PROCESSOR',
    4,
    0.50,   -- urgent = 2 hours
    'INDEXED'
);

-- Step 2: Route for Clinical Review
-- Triggered when document enters INDEXED. Assigned to CLAIMS_PROCESSOR.
-- SLA: 2 hours standard. Resolves to UNDER_REVIEW.
INSERT INTO workflow_step
    (id, workflow_definition_id, step_number, display_name, triggers_on_state,
     assigned_role_code, sla_hours, urgency_sla_multiplier, resolves_to_state)
VALUES (
    '00000000-0000-0000-0000-000000000512',
    '00000000-0000-0000-0000-000000000501',
    2,
    'Route for Clinical Review',
    'INDEXED',
    'CLAIMS_PROCESSOR',
    2,
    0.50,   -- urgent = 1 hour
    'UNDER_REVIEW'
);

-- Step 3: Clinical Review Decision
-- Triggered when document enters UNDER_REVIEW. Assigned to MEDICAL_DIRECTOR.
-- SLA: 24 hours standard, 6 hours urgent. Resolves to APPROVED (or DENIED via transition).
INSERT INTO workflow_step
    (id, workflow_definition_id, step_number, display_name, triggers_on_state,
     assigned_role_code, sla_hours, urgency_sla_multiplier, resolves_to_state)
VALUES (
    '00000000-0000-0000-0000-000000000513',
    '00000000-0000-0000-0000-000000000501',
    3,
    'Clinical Review Decision',
    'UNDER_REVIEW',
    'MEDICAL_DIRECTOR',
    24,
    0.25,   -- urgent = 6 hours (matches Redbook spec)
    'APPROVED'
);

-- Step 4: Notification and Filing
-- Triggered when document enters APPROVED. Assigned to CLAIMS_PROCESSOR.
-- SLA: 2 hours standard. Resolves to ARCHIVED.
INSERT INTO workflow_step
    (id, workflow_definition_id, step_number, display_name, triggers_on_state,
     assigned_role_code, sla_hours, urgency_sla_multiplier, resolves_to_state)
VALUES (
    '00000000-0000-0000-0000-000000000514',
    '00000000-0000-0000-0000-000000000501',
    4,
    'Notification and Filing',
    'APPROVED',
    'CLAIMS_PROCESSOR',
    2,
    0.50,   -- urgent = 1 hour
    'ARCHIVED'
);
