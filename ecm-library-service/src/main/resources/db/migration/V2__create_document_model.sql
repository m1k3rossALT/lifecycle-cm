-- ============================================================
-- V2 — Document Model
-- Creates: document, document_attribute, lifecycle_transition
-- Also creates the document_number sequence used by
-- DocumentService to assign human-readable document numbers.
-- ============================================================

-- ------------------------------------------------------------
-- Sequence for document number generation.
-- DocumentService formats this as: DOC-{YYYY}-{seq:07d}
-- e.g. DOC-2025-0000441
-- The sequence is intentionally not reset per year — monotonic
-- numbers are safer for audit trails than year-scoped ones.
-- ------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS document_number_seq
    START 1
    INCREMENT 1
    NO CYCLE;

-- ------------------------------------------------------------
-- document
-- The core repository record. Owns lifecycle state, a reference
-- to binary content in the resource manager, and versioning.
-- Metadata values are stored in document_attribute (EAV).
-- ------------------------------------------------------------
CREATE TABLE document (
    id                      UUID        NOT NULL DEFAULT gen_random_uuid(),
    document_number         VARCHAR(30) NOT NULL,
    document_class_id       UUID        NOT NULL,
    lifecycle_state         VARCHAR(30) NOT NULL,   -- LifecycleState enum
    -- content_reference_id points to content_object.id in ecm-resource-manager.
    -- Not a FK because it crosses service boundaries. Null after disposition.
    content_reference_id    UUID,
    version_label           VARCHAR(10) NOT NULL DEFAULT '1.0',
    is_current_version      BOOLEAN     NOT NULL DEFAULT TRUE,
    -- Self-referencing FK to the document this supersedes, if any.
    previous_version_id     UUID,
    created_by_user_id      VARCHAR(100),
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_document PRIMARY KEY (id),
    CONSTRAINT uq_document_number UNIQUE (document_number),
    CONSTRAINT fk_document_class FOREIGN KEY (document_class_id)
        REFERENCES document_class (id),
    CONSTRAINT fk_document_previous_version FOREIGN KEY (previous_version_id)
        REFERENCES document (id)
);

COMMENT ON TABLE  document                          IS 'Core document record: lifecycle state, class assignment, content reference, versioning';
COMMENT ON COLUMN document.content_reference_id    IS 'UUID of content_object in ecm-resource-manager. No FK: crosses service boundary. Null after disposition.';
COMMENT ON COLUMN document.version_label           IS '1.0 = initial, 1.x = minor revision (metadata), 2.0 = major revision (content replaced)';

-- Indexes for common query patterns
CREATE INDEX idx_document_class_id     ON document (document_class_id);
CREATE INDEX idx_document_state        ON document (lifecycle_state);
CREATE INDEX idx_document_created_at   ON document (created_at);
CREATE INDEX idx_document_current      ON document (is_current_version) WHERE is_current_version = TRUE;

-- ------------------------------------------------------------
-- document_attribute
-- EAV table storing all metadata field values for a document.
-- attribute_key matches attribute_definition.attribute_key.
-- One row per attribute field per document.
-- ------------------------------------------------------------
CREATE TABLE document_attribute (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    document_id     UUID        NOT NULL,
    attribute_key   VARCHAR(100) NOT NULL,
    attribute_value TEXT        NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_document_attribute PRIMARY KEY (id),
    CONSTRAINT fk_doc_attr_document FOREIGN KEY (document_id)
        REFERENCES document (id),
    -- A document cannot have two values for the same attribute key
    CONSTRAINT uq_document_attribute_key UNIQUE (document_id, attribute_key)
);

COMMENT ON TABLE  document_attribute               IS 'EAV storage for all document metadata field values';
COMMENT ON COLUMN document_attribute.attribute_key IS 'Must match an attribute_definition.attribute_key for the document class';

CREATE INDEX idx_doc_attr_document_id  ON document_attribute (document_id);
CREATE INDEX idx_doc_attr_key          ON document_attribute (attribute_key);
-- Composite index for the common query pattern: find documents by class + attribute value
CREATE INDEX idx_doc_attr_key_value    ON document_attribute (attribute_key, attribute_value);

-- ------------------------------------------------------------
-- lifecycle_transition
-- Immutable audit record of every state change. Never updated,
-- never deleted. The complete processing history of a document.
-- ------------------------------------------------------------
CREATE TABLE lifecycle_transition (
    id                      UUID        NOT NULL DEFAULT gen_random_uuid(),
    document_id             UUID        NOT NULL,
    -- from_state is null only for the initial RECEIVED transition
    from_state              VARCHAR(30),
    to_state                VARCHAR(30) NOT NULL,
    initiated_by_user_id    VARCHAR(100) NOT NULL,
    initiated_by_role       VARCHAR(50),
    transition_note         TEXT,
    client_ip_address       VARCHAR(45),    -- IPv4 or IPv6
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_lifecycle_transition PRIMARY KEY (id),
    CONSTRAINT fk_lt_document FOREIGN KEY (document_id)
        REFERENCES document (id)
);

COMMENT ON TABLE  lifecycle_transition                    IS 'Immutable audit log of every lifecycle state change. Never updated or deleted.';
COMMENT ON COLUMN lifecycle_transition.from_state         IS 'Null for the initial ingestion transition (no prior state)';
COMMENT ON COLUMN lifecycle_transition.initiated_by_user_id IS 'User ID or SYSTEM for automated transitions (Retention Engine, etc.)';
COMMENT ON COLUMN lifecycle_transition.client_ip_address  IS 'Captured for HIPAA traceability on PHI-bearing document transitions';

CREATE INDEX idx_lt_document_id ON lifecycle_transition (document_id);
CREATE INDEX idx_lt_created_at  ON lifecycle_transition (created_at);
