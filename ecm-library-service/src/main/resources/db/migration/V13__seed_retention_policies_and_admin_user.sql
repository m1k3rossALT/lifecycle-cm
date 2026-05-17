-- ============================================================
-- V13 — Seed: Retention Policies and Default Admin User
-- ============================================================

-- ------------------------------------------------------------
-- Retention Policies
-- ------------------------------------------------------------
INSERT INTO retention_policy
    (policy_code, display_name, regulatory_basis, trigger_event, trigger_attribute_key, retention_years, disposition_action)
VALUES
    ('CMS_MEDICARE_10YR',
     'CMS Medicare 10-Year Retention',
     '42 CFR §482.24 — CMS Medicare Conditions of Participation',
     'CLAIM_CLOSE_DATE',
     'claim_close_date',
     10,
     'DELETE'),

    ('HIPAA_PHI_6YR',
     'HIPAA PHI 6-Year Retention',
     '45 CFR §164.530(j) — HIPAA Privacy Rule',
     'AUTHORIZATION_DECISION_DATE',
     'auth_decision_date',
     6,
     'DELETE'),

    ('HIPAA_EOB_6YR',
     'HIPAA EOB 6-Year Retention',
     '45 CFR §164.530(j) — HIPAA Privacy Rule',
     'DOCUMENT_CREATION',
     -- EOB trigger is document creation date; no specific attribute key needed.
     -- The engine uses created_at from the document record when this value is present.
     'document_created_at',
     6,
     'DELETE'),

    ('ERISA_6YR',
     'ERISA 6-Year Enrollment Record Retention',
     'ERISA §107 — Employee Retirement Income Security Act',
     'MEMBER_TERMINATION_DATE',
     'member_termination_date',
     6,
     'LEGAL_HOLD_CHECK'),

    ('CMS_APPEAL_6YR',
     'CMS Appeal 6-Year Retention',
     '42 CFR §422.562 — CMS Medicare Advantage Appeals',
     'RESOLUTION_DATE',
     'resolution_date',
     6,
     'DELETE'),

    ('SOX_7YR',
     'Sarbanes-Oxley 7-Year Provider Contract Retention',
     'Sarbanes-Oxley Act §802 — Records Management',
     'CONTRACT_TERMINATION_DATE',
     'termination_date',
     7,
     'DELETE');

-- ------------------------------------------------------------
-- Default SYSTEM_ADMIN user
-- Password hash is bcrypt of 'ChangeMe123!' — must be changed
-- before any production or shared environment use.
-- The 'NOT RESPONSIBLE FOR' comment below is intentional:
-- this seed exists only to bootstrap the system. Password
-- rotation is owned by the operator.
-- ------------------------------------------------------------
INSERT INTO ecm_user (id, username, password_hash, full_name, email, scope_type, is_active)
VALUES (
    '00000000-0000-0000-0000-000000000401',
    'system_admin',
    -- bcrypt hash of 'ChangeMe123!' — CHANGE BEFORE PRODUCTION
    '$2a$12$Kp8G9XZqLvD3mN7wRtYuOeJ5hF6kA2sB4cE8dI1jM0nP7qS9tV3xW',
    'System Administrator',
    'admin@lifecyclecm.local',
    'ALL',
    TRUE
);

-- Assign SYSTEM_ADMIN role to the default user
INSERT INTO user_role (user_id, role_id, effective_from, assigned_by_user_id)
VALUES (
    '00000000-0000-0000-0000-000000000401',
    '00000000-0000-0000-0000-000000000307',
    CURRENT_DATE,
    'SEED'
);
