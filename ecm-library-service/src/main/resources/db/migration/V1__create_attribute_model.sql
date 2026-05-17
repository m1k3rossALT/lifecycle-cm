-- ============================================================
-- V1 — Attribute Model
-- Creates the schema-driven metadata foundation:
--   attribute_group, attribute_definition, document_class,
--   document_class_attribute_group
--
-- Design principle: the document schema is defined in data,
-- not in code. New document types and attribute fields are
-- added by administrators through seed data or admin console,
-- not by code changes or deployments.
-- ============================================================

-- ------------------------------------------------------------
-- attribute_group
-- A named, reusable collection of typed attribute definitions.
-- Multiple document classes can reference the same group.
-- ------------------------------------------------------------
CREATE TABLE attribute_group (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    code            VARCHAR(50) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_attribute_group PRIMARY KEY (id),
    CONSTRAINT uq_attribute_group_code UNIQUE (code)
);

COMMENT ON TABLE  attribute_group         IS 'Reusable named collections of typed attribute definitions';
COMMENT ON COLUMN attribute_group.code    IS 'Machine-readable identifier, e.g. MEMBER_ATTRIBUTES';

-- ------------------------------------------------------------
-- attribute_definition
-- A single typed metadata field within an attribute group.
-- Defines the data type, validation rules, and display order.
-- ------------------------------------------------------------
CREATE TABLE attribute_definition (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    attribute_group_id  UUID        NOT NULL,
    attribute_key       VARCHAR(100) NOT NULL,
    display_name        VARCHAR(100) NOT NULL,
    data_type           VARCHAR(20) NOT NULL,   -- AttributeDataType enum
    required            BOOLEAN     NOT NULL DEFAULT FALSE,
    max_length          INTEGER,                -- applies to STRING type only
    allowed_values      TEXT,                  -- comma-separated list for ENUM type
    is_retention_trigger BOOLEAN    NOT NULL DEFAULT FALSE,
    display_order       INTEGER     NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_attribute_definition PRIMARY KEY (id),
    CONSTRAINT fk_attr_def_group FOREIGN KEY (attribute_group_id)
        REFERENCES attribute_group (id),
    -- An attribute_key must be unique within its group
    CONSTRAINT uq_attr_def_key_per_group UNIQUE (attribute_group_id, attribute_key)
);

COMMENT ON TABLE  attribute_definition                    IS 'Individual typed metadata field definitions within an attribute group';
COMMENT ON COLUMN attribute_definition.attribute_key      IS 'Machine-readable key used in document_attribute EAV rows, e.g. member_id';
COMMENT ON COLUMN attribute_definition.is_retention_trigger IS 'True if this field holds a date used as a retention clock start point';

-- ------------------------------------------------------------
-- document_class
-- Defines a type of repository document. Controls which
-- attribute groups apply, PHI status, and retention policy.
-- ------------------------------------------------------------
CREATE TABLE document_class (
    id                      UUID        NOT NULL DEFAULT gen_random_uuid(),
    code                    VARCHAR(50) NOT NULL,
    display_name            VARCHAR(100) NOT NULL,
    description             TEXT,
    is_phi_bearing          BOOLEAN     NOT NULL DEFAULT FALSE,
    is_active               BOOLEAN     NOT NULL DEFAULT TRUE,
    -- retention_policy_code is a plain string reference here;
    -- the FK to retention_policy is added in V4 once that table exists.
    retention_policy_code   VARCHAR(50),
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_document_class PRIMARY KEY (id),
    CONSTRAINT uq_document_class_code UNIQUE (code)
);

COMMENT ON TABLE  document_class                    IS 'Defines a document type: schema, PHI status, retention policy';
COMMENT ON COLUMN document_class.retention_policy_code IS 'References retention_policy.code; FK constraint added in V4';

-- ------------------------------------------------------------
-- document_class_attribute_group
-- Join table linking document classes to their attribute groups.
-- display_order controls the rendering sequence in forms and views.
-- ------------------------------------------------------------
CREATE TABLE document_class_attribute_group (
    id                  UUID    NOT NULL DEFAULT gen_random_uuid(),
    document_class_id   UUID    NOT NULL,
    attribute_group_id  UUID    NOT NULL,
    display_order       INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_dc_attribute_group PRIMARY KEY (id),
    CONSTRAINT fk_dcag_document_class FOREIGN KEY (document_class_id)
        REFERENCES document_class (id),
    CONSTRAINT fk_dcag_attribute_group FOREIGN KEY (attribute_group_id)
        REFERENCES attribute_group (id),
    CONSTRAINT uq_dcag_pair UNIQUE (document_class_id, attribute_group_id)
);

COMMENT ON TABLE document_class_attribute_group IS 'Links attribute groups to document classes with display ordering';
