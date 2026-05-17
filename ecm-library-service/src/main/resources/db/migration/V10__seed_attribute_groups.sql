-- ============================================================
-- V10 — Seed: Attribute Groups and Definitions
-- Seeds all 8 healthcare attribute groups and every attribute
-- field within each group.
--
-- UUIDs are fixed so they can be referenced reliably in V11
-- (document class mappings) and in tests.
-- ============================================================

-- ------------------------------------------------------------
-- Attribute Groups
-- ------------------------------------------------------------
INSERT INTO attribute_group (id, code, display_name, description) VALUES
    ('00000000-0000-0000-0000-000000000101',
     'MEMBER_ATTRIBUTES',
     'Member Attributes',
     'Member demographic and plan enrollment identifiers. Shared across all PHI-bearing document classes.'),

    ('00000000-0000-0000-0000-000000000102',
     'PROVIDER_ATTRIBUTES',
     'Provider Attributes',
     'Healthcare provider identification and network status fields.'),

    ('00000000-0000-0000-0000-000000000103',
     'CLAIM_ATTRIBUTES',
     'Claim Attributes',
     'Medical claim processing fields including service dates, claim type, and billing amounts.'),

    ('00000000-0000-0000-0000-000000000104',
     'AUTHORIZATION_ATTRIBUTES',
     'Authorization Attributes',
     'Prior authorization request fields including service code, urgency, and decision date.'),

    ('00000000-0000-0000-0000-000000000105',
     'PROCESSING_ATTRIBUTES',
     'Processing Attributes',
     'Operational fields tracking how and when a document was received and routed.'),

    ('00000000-0000-0000-0000-000000000106',
     'CONTRACT_ATTRIBUTES',
     'Contract Attributes',
     'Provider contract fields including effective dates, contract type, and contracting entity.'),

    ('00000000-0000-0000-0000-000000000107',
     'ENROLLMENT_ATTRIBUTES',
     'Enrollment Attributes',
     'Member enrollment and coverage fields used on enrollment forms and coverage change documents.'),

    ('00000000-0000-0000-0000-000000000108',
     'APPEAL_ATTRIBUTES',
     'Appeal Attributes',
     'Appeal and grievance fields including appeal type, reason code, and resolution date.');

-- ------------------------------------------------------------
-- Attribute Definitions — MEMBER_ATTRIBUTES
-- ------------------------------------------------------------
INSERT INTO attribute_definition
    (attribute_group_id, attribute_key, display_name, data_type, required, max_length, allowed_values, is_retention_trigger, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000101',
     'member_id', 'Member ID', 'STRING', TRUE, 20, NULL, FALSE, 1),

    ('00000000-0000-0000-0000-000000000101',
     'member_name', 'Member Name', 'STRING', TRUE, 100, NULL, FALSE, 2),

    ('00000000-0000-0000-0000-000000000101',
     'date_of_birth', 'Date of Birth', 'DATE', TRUE, NULL, NULL, FALSE, 3),

    ('00000000-0000-0000-0000-000000000101',
     'plan_id', 'Plan ID', 'STRING', TRUE, 20, NULL, FALSE, 4),

    ('00000000-0000-0000-0000-000000000101',
     'group_number', 'Group Number', 'STRING', FALSE, 20, NULL, FALSE, 5),

    -- Retention trigger for ERISA_6YR (MEMBER_ENROLLMENT_FORM)
    ('00000000-0000-0000-0000-000000000101',
     'member_termination_date', 'Member Termination Date', 'DATE', FALSE, NULL, NULL, TRUE, 6);

-- ------------------------------------------------------------
-- Attribute Definitions — PROVIDER_ATTRIBUTES
-- ------------------------------------------------------------
INSERT INTO attribute_definition
    (attribute_group_id, attribute_key, display_name, data_type, required, max_length, allowed_values, is_retention_trigger, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000102',
     'provider_npi', 'Provider NPI', 'STRING', TRUE, 10, NULL, FALSE, 1),

    ('00000000-0000-0000-0000-000000000102',
     'provider_name', 'Provider Name', 'STRING', TRUE, 100, NULL, FALSE, 2),

    ('00000000-0000-0000-0000-000000000102',
     'provider_tin', 'Provider TIN', 'STRING', FALSE, 9, NULL, FALSE, 3),

    ('00000000-0000-0000-0000-000000000102',
     'network_status', 'Network Status', 'ENUM', TRUE, NULL,
     'IN_NETWORK,OUT_OF_NETWORK,UNKNOWN', FALSE, 4);

-- ------------------------------------------------------------
-- Attribute Definitions — CLAIM_ATTRIBUTES
-- ------------------------------------------------------------
INSERT INTO attribute_definition
    (attribute_group_id, attribute_key, display_name, data_type, required, max_length, allowed_values, is_retention_trigger, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000103',
     'claim_number', 'Claim Number', 'STRING', TRUE, 30, NULL, FALSE, 1),

    ('00000000-0000-0000-0000-000000000103',
     'service_date_from', 'Service Date From', 'DATE', TRUE, NULL, NULL, TRUE, 2),

    ('00000000-0000-0000-0000-000000000103',
     'service_date_to', 'Service Date To', 'DATE', FALSE, NULL, NULL, FALSE, 3),

    ('00000000-0000-0000-0000-000000000103',
     'claim_type', 'Claim Type', 'ENUM', TRUE, NULL,
     'PROFESSIONAL,INSTITUTIONAL,DENTAL,VISION', FALSE, 4),

    ('00000000-0000-0000-0000-000000000103',
     'total_billed_amount', 'Total Billed Amount', 'NUMBER', TRUE, NULL, NULL, FALSE, 5),

    ('00000000-0000-0000-0000-000000000103',
     'diagnosis_code_primary', 'Primary Diagnosis Code', 'STRING', TRUE, 10, NULL, FALSE, 6),

    -- Retention trigger for CMS_MEDICARE_10YR (MEDICAL_CLAIM)
    ('00000000-0000-0000-0000-000000000103',
     'claim_close_date', 'Claim Close Date', 'DATE', FALSE, NULL, NULL, TRUE, 7);

-- ------------------------------------------------------------
-- Attribute Definitions — AUTHORIZATION_ATTRIBUTES
-- ------------------------------------------------------------
INSERT INTO attribute_definition
    (attribute_group_id, attribute_key, display_name, data_type, required, max_length, allowed_values, is_retention_trigger, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000104',
     'auth_number', 'Authorization Number', 'STRING', TRUE, 30, NULL, FALSE, 1),

    ('00000000-0000-0000-0000-000000000104',
     'requested_service_code', 'Requested Service Code', 'STRING', TRUE, 20, NULL, FALSE, 2),

    ('00000000-0000-0000-0000-000000000104',
     'requested_units', 'Requested Units', 'NUMBER', TRUE, NULL, NULL, FALSE, 3),

    ('00000000-0000-0000-0000-000000000104',
     'urgency_level', 'Urgency Level', 'ENUM', TRUE, NULL,
     'STANDARD,URGENT,EXPEDITED', FALSE, 4),

    ('00000000-0000-0000-0000-000000000104',
     'requesting_provider_npi', 'Requesting Provider NPI', 'STRING', TRUE, 10, NULL, FALSE, 5),

    -- Retention trigger for HIPAA_PHI_6YR (PRIOR_AUTHORIZATION_REQUEST)
    ('00000000-0000-0000-0000-000000000104',
     'auth_decision_date', 'Authorization Decision Date', 'DATE', FALSE, NULL, NULL, TRUE, 6);

-- ------------------------------------------------------------
-- Attribute Definitions — PROCESSING_ATTRIBUTES
-- ------------------------------------------------------------
INSERT INTO attribute_definition
    (attribute_group_id, attribute_key, display_name, data_type, required, max_length, allowed_values, is_retention_trigger, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000105',
     'received_date', 'Received Date', 'DATE', TRUE, NULL, NULL, FALSE, 1),

    ('00000000-0000-0000-0000-000000000105',
     'received_channel', 'Received Channel', 'ENUM', TRUE, NULL,
     'EDI,PORTAL,FAX,MAIL,PHONE', FALSE, 2),

    ('00000000-0000-0000-0000-000000000105',
     'assigned_queue', 'Assigned Queue', 'STRING', FALSE, 50, NULL, FALSE, 3),

    ('00000000-0000-0000-0000-000000000105',
     'processing_region', 'Processing Region', 'STRING', FALSE, 20, NULL, FALSE, 4),

    ('00000000-0000-0000-0000-000000000105',
     'indexer_user_id', 'Indexer User ID', 'STRING', FALSE, 50, NULL, FALSE, 5);

-- ------------------------------------------------------------
-- Attribute Definitions — CONTRACT_ATTRIBUTES
-- ------------------------------------------------------------
INSERT INTO attribute_definition
    (attribute_group_id, attribute_key, display_name, data_type, required, max_length, allowed_values, is_retention_trigger, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000106',
     'contract_number', 'Contract Number', 'STRING', TRUE, 30, NULL, FALSE, 1),

    ('00000000-0000-0000-0000-000000000106',
     'effective_date', 'Effective Date', 'DATE', TRUE, NULL, NULL, FALSE, 2),

    -- Retention trigger for SOX_7YR (PROVIDER_CONTRACT)
    ('00000000-0000-0000-0000-000000000106',
     'termination_date', 'Termination Date', 'DATE', FALSE, NULL, NULL, TRUE, 3),

    ('00000000-0000-0000-0000-000000000106',
     'contract_type', 'Contract Type', 'ENUM', TRUE, NULL,
     'FEE_FOR_SERVICE,CAPITATION,VALUE_BASED,ADMINISTRATIVE', FALSE, 4),

    ('00000000-0000-0000-0000-000000000106',
     'contracting_entity', 'Contracting Entity', 'STRING', TRUE, 100, NULL, FALSE, 5);

-- ------------------------------------------------------------
-- Attribute Definitions — ENROLLMENT_ATTRIBUTES
-- ------------------------------------------------------------
INSERT INTO attribute_definition
    (attribute_group_id, attribute_key, display_name, data_type, required, max_length, allowed_values, is_retention_trigger, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000107',
     'enrollment_effective_date', 'Enrollment Effective Date', 'DATE', TRUE, NULL, NULL, FALSE, 1),

    ('00000000-0000-0000-0000-000000000107',
     'coverage_type', 'Coverage Type', 'ENUM', TRUE, NULL,
     'INDIVIDUAL,FAMILY,EMPLOYEE_ONLY,EMPLOYEE_SPOUSE,EMPLOYEE_CHILD', FALSE, 2),

    ('00000000-0000-0000-0000-000000000107',
     'employer_group_id', 'Employer Group ID', 'STRING', FALSE, 30, NULL, FALSE, 3),

    ('00000000-0000-0000-0000-000000000107',
     'enrollment_source', 'Enrollment Source', 'ENUM', FALSE, NULL,
     'EMPLOYER_GROUP,MARKETPLACE,MEDICARE,MEDICAID,COBRA', FALSE, 4);

-- ------------------------------------------------------------
-- Attribute Definitions — APPEAL_ATTRIBUTES
-- ------------------------------------------------------------
INSERT INTO attribute_definition
    (attribute_group_id, attribute_key, display_name, data_type, required, max_length, allowed_values, is_retention_trigger, display_order)
VALUES
    ('00000000-0000-0000-0000-000000000108',
     'appeal_number', 'Appeal Number', 'STRING', TRUE, 30, NULL, FALSE, 1),

    ('00000000-0000-0000-0000-000000000108',
     'original_claim_number', 'Original Claim Number', 'STRING', FALSE, 30, NULL, FALSE, 2),

    ('00000000-0000-0000-0000-000000000108',
     'appeal_type', 'Appeal Type', 'ENUM', TRUE, NULL,
     'FIRST_LEVEL,SECOND_LEVEL,EXTERNAL', FALSE, 3),

    ('00000000-0000-0000-0000-000000000108',
     'appeal_reason_code', 'Appeal Reason Code', 'STRING', TRUE, 20, NULL, FALSE, 4),

    -- Retention trigger for CMS_APPEAL_6YR (APPEAL_AND_GRIEVANCE)
    ('00000000-0000-0000-0000-000000000108',
     'resolution_date', 'Resolution Date', 'DATE', FALSE, NULL, NULL, TRUE, 5);
     