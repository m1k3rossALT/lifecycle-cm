-- ============================================================
-- V4 — Retention and Disposition
-- Creates: retention_policy, retention_schedule,
--          legal_hold, legal_hold_document
--
-- Also adds the FK constraint from document_class to
-- retention_policy now that both tables exist.
-- ============================================================

-- ------------------------------------------------------------
-- retention_policy
-- Defines the rules governing how long a document class must
-- be retained and what happens at eligibility.
-- ------------------------------------------------------------
CREATE TABLE retention_policy (
    id                      UUID        NOT NULL DEFAULT gen_random_uuid(),
    policy_code             VARCHAR(50) NOT NULL,
    display_name            VARCHAR(150) NOT NULL,
    regulatory_basis        TEXT        NOT NULL,
    -- trigger_event matches RetentionTriggerEvent enum
    trigger_event           VARCHAR(50) NOT NULL,
    -- trigger_attribute_key: the attribute_key the engine reads to get the trigger date
    -- e.g. 'claim_close_date' for CMS_MEDICARE_10YR
    trigger_attribute_key   VARCHAR(100) NOT NULL,
    retention_years         INTEGER     NOT NULL,
    -- disposition_action matches DispositionAction enum
    disposition_action      VARCHAR(30) NOT NULL DEFAULT 'DELETE',
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_retention_policy PRIMARY KEY (id),
    CONSTRAINT uq_retention_policy_code UNIQUE (policy_code)
);

COMMENT ON TABLE  retention_policy                    IS 'Defines retention rules: regulatory basis, clock trigger, period, and disposition action.';
COMMENT ON COLUMN retention_policy.trigger_attribute_key IS 'The attribute_definition.attribute_key whose value provides the retention clock start date.';

-- Now that retention_policy exists, add the FK from document_class
ALTER TABLE document_class
    ADD CONSTRAINT fk_dc_retention_policy
    FOREIGN KEY (retention_policy_code)
    REFERENCES retention_policy (policy_code);

-- ------------------------------------------------------------
-- retention_schedule
-- Computed retention schedule for a specific archived document.
-- Created by the Retention Engine when a document reaches ARCHIVED.
-- One record per document (multi-policy conflict resolved to one row).
-- ------------------------------------------------------------
CREATE TABLE retention_schedule (
    id                              UUID    NOT NULL DEFAULT gen_random_uuid(),
    document_id                     UUID    NOT NULL,
    policy_code                     VARCHAR(50) NOT NULL,
    trigger_attribute_key           VARCHAR(100) NOT NULL,
    trigger_date                    DATE    NOT NULL,
    eligible_for_disposition_date   DATE    NOT NULL,
    -- status matches a subset of values: SCHEDULED, ELIGIBLE, ON_LEGAL_HOLD, DISPOSED
    status                          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    computed_at                     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_retention_schedule PRIMARY KEY (id),
    CONSTRAINT fk_rs_document FOREIGN KEY (document_id) REFERENCES document (id),
    CONSTRAINT uq_retention_schedule_document UNIQUE (document_id)
);

COMMENT ON TABLE  retention_schedule IS 'Computed retention schedule per document. One row per document after archival.';
COMMENT ON COLUMN retention_schedule.status IS 'SCHEDULED | ELIGIBLE | ON_LEGAL_HOLD | DISPOSED';

CREATE INDEX idx_rs_status              ON retention_schedule (status);
CREATE INDEX idx_rs_eligible_date       ON retention_schedule (eligible_for_disposition_date);

-- ------------------------------------------------------------
-- legal_hold
-- A formal instruction to suspend normal retention and disposition
-- for a defined set of documents. Placed by Compliance Officers.
-- ------------------------------------------------------------
CREATE TABLE legal_hold (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    hold_identifier VARCHAR(50) NOT NULL,   -- e.g. LH-2025-CMS-0041
    hold_type       VARCHAR(30) NOT NULL,   -- LITIGATION | REGULATORY_INQUIRY | INTERNAL_AUDIT
    reason          TEXT        NOT NULL,
    placed_by_user_id VARCHAR(100) NOT NULL,
    placed_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    released_by_user_id VARCHAR(100),
    released_at     TIMESTAMP WITH TIME ZONE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_legal_hold PRIMARY KEY (id),
    CONSTRAINT uq_legal_hold_identifier UNIQUE (hold_identifier)
);

COMMENT ON TABLE  legal_hold            IS 'Active and historical legal holds placed by Compliance Officers.';
COMMENT ON COLUMN legal_hold.is_active  IS 'False once released. Records are never deleted.';

CREATE INDEX idx_legal_hold_active ON legal_hold (is_active) WHERE is_active = TRUE;

-- ------------------------------------------------------------
-- legal_hold_document
-- Join table linking a legal hold to the documents it covers.
-- A hold can cover many documents; a document can be under
-- multiple simultaneous holds.
-- ------------------------------------------------------------
CREATE TABLE legal_hold_document (
    id              UUID    NOT NULL DEFAULT gen_random_uuid(),
    legal_hold_id   UUID    NOT NULL,
    document_id     UUID    NOT NULL,
    added_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_legal_hold_document PRIMARY KEY (id),
    CONSTRAINT fk_lhd_hold FOREIGN KEY (legal_hold_id) REFERENCES legal_hold (id),
    CONSTRAINT fk_lhd_document FOREIGN KEY (document_id) REFERENCES document (id),
    CONSTRAINT uq_legal_hold_document_pair UNIQUE (legal_hold_id, document_id)
);

CREATE INDEX idx_lhd_document_id   ON legal_hold_document (document_id);
CREATE INDEX idx_lhd_hold_id       ON legal_hold_document (legal_hold_id);
