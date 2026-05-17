-- ============================================================
-- V11 — Seed: Document Classes and Attribute Group Mappings
-- Seeds all 6 healthcare document classes and links them to
-- their governing attribute groups in display order.
--
-- retention_policy_code is stored as a string here; the FK
-- constraint to retention_policy is added when V4 runs.
-- ============================================================

-- ------------------------------------------------------------
-- Document Classes
-- ------------------------------------------------------------
INSERT INTO document_class
    (id, code, display_name, description, is_phi_bearing, is_active, retention_policy_code)
VALUES
    ('00000000-0000-0000-0000-000000000201',
     'MEDICAL_CLAIM',
     'Medical Claim',
     'A formal request by a provider for reimbursement of healthcare services rendered to a member.',
     TRUE, TRUE, 'CMS_MEDICARE_10YR'),

    ('00000000-0000-0000-0000-000000000202',
     'PRIOR_AUTHORIZATION_REQUEST',
     'Prior Authorization Request',
     'A request submitted by a provider seeking pre-approval for a specific service, medication, or procedure.',
     TRUE, TRUE, 'HIPAA_PHI_6YR'),

    ('00000000-0000-0000-0000-000000000203',
     'EXPLANATION_OF_BENEFITS',
     'Explanation of Benefits',
     'A summary statement issued to members detailing how a claim was processed and what was paid.',
     TRUE, TRUE, 'HIPAA_EOB_6YR'),

    ('00000000-0000-0000-0000-000000000204',
     'MEMBER_ENROLLMENT_FORM',
     'Member Enrollment Form',
     'The application or change form submitted when a member enrolls in or modifies their health plan coverage.',
     TRUE, TRUE, 'ERISA_6YR'),

    ('00000000-0000-0000-0000-000000000205',
     'APPEAL_AND_GRIEVANCE',
     'Appeal and Grievance',
     'A formal challenge filed by a member or provider contesting a claim decision or coverage denial.',
     TRUE, TRUE, 'CMS_APPEAL_6YR'),

    ('00000000-0000-0000-0000-000000000206',
     'PROVIDER_CONTRACT',
     'Provider Contract',
     'A contractual agreement between the organization and a healthcare provider or provider group.',
     FALSE, TRUE, 'SOX_7YR');

-- ------------------------------------------------------------
-- Document Class — Attribute Group Mappings
--
-- MEDICAL_CLAIM: Member, Provider, Claim, Processing
-- PRIOR_AUTHORIZATION_REQUEST: Member, Provider, Authorization, Processing
-- EXPLANATION_OF_BENEFITS: Member, Claim
-- MEMBER_ENROLLMENT_FORM: Member, Enrollment, Processing
-- APPEAL_AND_GRIEVANCE: Member, Appeal, Processing
-- PROVIDER_CONTRACT: Provider, Contract
-- ------------------------------------------------------------

-- MEDICAL_CLAIM
INSERT INTO document_class_attribute_group
    (document_class_id, attribute_group_id, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000101', 1),  -- MEMBER
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000102', 2),  -- PROVIDER
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000103', 3),  -- CLAIM
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000105', 4);  -- PROCESSING

-- PRIOR_AUTHORIZATION_REQUEST
INSERT INTO document_class_attribute_group
    (document_class_id, attribute_group_id, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000101', 1),  -- MEMBER
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000102', 2),  -- PROVIDER
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000104', 3),  -- AUTHORIZATION
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000105', 4);  -- PROCESSING

-- EXPLANATION_OF_BENEFITS
INSERT INTO document_class_attribute_group
    (document_class_id, attribute_group_id, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000101', 1),  -- MEMBER
    ('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000103', 2);  -- CLAIM

-- MEMBER_ENROLLMENT_FORM
INSERT INTO document_class_attribute_group
    (document_class_id, attribute_group_id, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000101', 1),  -- MEMBER
    ('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000107', 2),  -- ENROLLMENT
    ('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000105', 3);  -- PROCESSING

-- APPEAL_AND_GRIEVANCE
INSERT INTO document_class_attribute_group
    (document_class_id, attribute_group_id, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000101', 1),  -- MEMBER
    ('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000108', 2),  -- APPEAL
    ('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000105', 3);  -- PROCESSING

-- PROVIDER_CONTRACT
INSERT INTO document_class_attribute_group
    (document_class_id, attribute_group_id, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000102', 1),  -- PROVIDER
    ('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000106', 2);  -- CONTRACT
    