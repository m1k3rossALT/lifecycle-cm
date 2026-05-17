-- ============================================================
-- V12 — Seed: Roles and Permission Matrix
-- Seeds all 7 system roles and their permission rows for
-- every document class.
--
-- Permission design principles:
--   CLAIMS_PROCESSOR   — create + transition on operational docs
--   MEDICAL_DIRECTOR   — transition on clinical docs (no create)
--   MEMBER_SERVICES    — read + create on member-facing docs
--   PROVIDER_RELATIONS — read + create on provider docs
--   COMPLIANCE_OFFICER — full read + transition + dispose everywhere
--   AUDITOR            — read-only everywhere, no transitions
--   SYSTEM_ADMIN       — full access everywhere
-- ============================================================

-- ------------------------------------------------------------
-- Roles
-- ------------------------------------------------------------
INSERT INTO ecm_role (id, code, display_name, description) VALUES
    ('00000000-0000-0000-0000-000000000301',
     'CLAIMS_PROCESSOR',
     'Claims Processor',
     'Processes incoming claims and prior authorization requests from an assigned task queue.'),

    ('00000000-0000-0000-0000-000000000302',
     'MEDICAL_DIRECTOR',
     'Medical Director',
     'Performs clinical review of prior authorization requests and complex claims.'),

    ('00000000-0000-0000-0000-000000000303',
     'MEMBER_SERVICES_REPRESENTATIVE',
     'Member Services Representative',
     'Assists members with enrollment, EOB queries, and appeal submissions.'),

    ('00000000-0000-0000-0000-000000000304',
     'PROVIDER_RELATIONS',
     'Provider Relations',
     'Manages provider contracts and resolves provider-facing disputes.'),

    ('00000000-0000-0000-0000-000000000305',
     'COMPLIANCE_OFFICER',
     'Compliance Officer',
     'Full read access across all document classes. Manages legal holds, approves dispositions, reviews PHI access logs.'),

    ('00000000-0000-0000-0000-000000000306',
     'AUDITOR',
     'Auditor',
     'Read-only access across all document classes for audit and reporting. No write permissions.'),

    ('00000000-0000-0000-0000-000000000307',
     'SYSTEM_ADMIN',
     'System Administrator',
     'Full system access including user management and document class configuration. Not a processing role.');

-- ------------------------------------------------------------
-- Permission Matrix
-- Columns: role_id | document_class_code | read | create | transition | dispose
-- ------------------------------------------------------------

-- CLAIMS_PROCESSOR
INSERT INTO role_permission (role_id, document_class_code, can_read, can_create, can_transition, can_dispose)
VALUES
    ('00000000-0000-0000-0000-000000000301', 'MEDICAL_CLAIM',               TRUE,  TRUE,  TRUE,  FALSE),
    ('00000000-0000-0000-0000-000000000301', 'PRIOR_AUTHORIZATION_REQUEST', TRUE,  TRUE,  TRUE,  FALSE),
    ('00000000-0000-0000-0000-000000000301', 'EXPLANATION_OF_BENEFITS',     TRUE,  TRUE,  FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000301', 'MEMBER_ENROLLMENT_FORM',      FALSE, FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000301', 'APPEAL_AND_GRIEVANCE',        TRUE,  FALSE, TRUE,  FALSE),
    ('00000000-0000-0000-0000-000000000301', 'PROVIDER_CONTRACT',           FALSE, FALSE, FALSE, FALSE);

-- MEDICAL_DIRECTOR
INSERT INTO role_permission (role_id, document_class_code, can_read, can_create, can_transition, can_dispose)
VALUES
    ('00000000-0000-0000-0000-000000000302', 'MEDICAL_CLAIM',               TRUE,  FALSE, TRUE,  FALSE),
    ('00000000-0000-0000-0000-000000000302', 'PRIOR_AUTHORIZATION_REQUEST', TRUE,  FALSE, TRUE,  FALSE),
    ('00000000-0000-0000-0000-000000000302', 'EXPLANATION_OF_BENEFITS',     TRUE,  FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000302', 'MEMBER_ENROLLMENT_FORM',      FALSE, FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000302', 'APPEAL_AND_GRIEVANCE',        TRUE,  FALSE, TRUE,  FALSE),
    ('00000000-0000-0000-0000-000000000302', 'PROVIDER_CONTRACT',           FALSE, FALSE, FALSE, FALSE);

-- MEMBER_SERVICES_REPRESENTATIVE
INSERT INTO role_permission (role_id, document_class_code, can_read, can_create, can_transition, can_dispose)
VALUES
    ('00000000-0000-0000-0000-000000000303', 'MEDICAL_CLAIM',               FALSE, FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000303', 'PRIOR_AUTHORIZATION_REQUEST', FALSE, FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000303', 'EXPLANATION_OF_BENEFITS',     TRUE,  FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000303', 'MEMBER_ENROLLMENT_FORM',      TRUE,  TRUE,  TRUE,  FALSE),
    ('00000000-0000-0000-0000-000000000303', 'APPEAL_AND_GRIEVANCE',        TRUE,  TRUE,  TRUE,  FALSE),
    ('00000000-0000-0000-0000-000000000303', 'PROVIDER_CONTRACT',           FALSE, FALSE, FALSE, FALSE);

-- PROVIDER_RELATIONS
INSERT INTO role_permission (role_id, document_class_code, can_read, can_create, can_transition, can_dispose)
VALUES
    ('00000000-0000-0000-0000-000000000304', 'MEDICAL_CLAIM',               FALSE, FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000304', 'PRIOR_AUTHORIZATION_REQUEST', FALSE, FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000304', 'EXPLANATION_OF_BENEFITS',     FALSE, FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000304', 'MEMBER_ENROLLMENT_FORM',      FALSE, FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000304', 'APPEAL_AND_GRIEVANCE',        FALSE, FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000304', 'PROVIDER_CONTRACT',           TRUE,  TRUE,  TRUE,  FALSE);

-- COMPLIANCE_OFFICER — read + transition + dispose everywhere
INSERT INTO role_permission (role_id, document_class_code, can_read, can_create, can_transition, can_dispose)
VALUES
    ('00000000-0000-0000-0000-000000000305', 'MEDICAL_CLAIM',               TRUE,  FALSE, TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000305', 'PRIOR_AUTHORIZATION_REQUEST', TRUE,  FALSE, TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000305', 'EXPLANATION_OF_BENEFITS',     TRUE,  FALSE, TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000305', 'MEMBER_ENROLLMENT_FORM',      TRUE,  FALSE, TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000305', 'APPEAL_AND_GRIEVANCE',        TRUE,  FALSE, TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000305', 'PROVIDER_CONTRACT',           TRUE,  FALSE, TRUE,  TRUE);

-- AUDITOR — read-only everywhere
INSERT INTO role_permission (role_id, document_class_code, can_read, can_create, can_transition, can_dispose)
VALUES
    ('00000000-0000-0000-0000-000000000306', 'MEDICAL_CLAIM',               TRUE,  FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000306', 'PRIOR_AUTHORIZATION_REQUEST', TRUE,  FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000306', 'EXPLANATION_OF_BENEFITS',     TRUE,  FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000306', 'MEMBER_ENROLLMENT_FORM',      TRUE,  FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000306', 'APPEAL_AND_GRIEVANCE',        TRUE,  FALSE, FALSE, FALSE),
    ('00000000-0000-0000-0000-000000000306', 'PROVIDER_CONTRACT',           TRUE,  FALSE, FALSE, FALSE);

-- SYSTEM_ADMIN — full access everywhere
INSERT INTO role_permission (role_id, document_class_code, can_read, can_create, can_transition, can_dispose)
VALUES
    ('00000000-0000-0000-0000-000000000307', 'MEDICAL_CLAIM',               TRUE,  TRUE,  TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000307', 'PRIOR_AUTHORIZATION_REQUEST', TRUE,  TRUE,  TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000307', 'EXPLANATION_OF_BENEFITS',     TRUE,  TRUE,  TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000307', 'MEMBER_ENROLLMENT_FORM',      TRUE,  TRUE,  TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000307', 'APPEAL_AND_GRIEVANCE',        TRUE,  TRUE,  TRUE,  TRUE),
    ('00000000-0000-0000-0000-000000000307', 'PROVIDER_CONTRACT',           TRUE,  TRUE,  TRUE,  TRUE);
    