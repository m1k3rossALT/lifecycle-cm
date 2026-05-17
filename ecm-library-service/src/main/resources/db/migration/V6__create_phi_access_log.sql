-- ============================================================
-- V6 — PHI Access Log
-- Creates: phi_access_log
--
-- This table is the primary HIPAA compliance evidence.
-- Every access to a PHI-bearing document — search result,
-- detail view, content download — produces one immutable row.
-- No updates. No deletes. Ever.
-- ============================================================

CREATE TABLE phi_access_log (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    document_id     UUID        NOT NULL,
    document_number VARCHAR(30) NOT NULL,   -- denormalized for fast audit queries
    document_class_code VARCHAR(50) NOT NULL, -- denormalized for fast audit queries
    accessed_by_user_id VARCHAR(100) NOT NULL,
    accessed_by_role    VARCHAR(50),
    -- access_type: VIEW_METADATA | DOWNLOAD_CONTENT | SEARCH_RESULT
    access_type     VARCHAR(30) NOT NULL,
    -- access_reason: derived from role and operation, e.g. CLAIMS_PROCESSING
    access_reason   VARCHAR(100),
    client_ip_address VARCHAR(45),
    accessed_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_phi_access_log PRIMARY KEY (id),
    -- No FK to document — intentional. If a document is disposed and
    -- the record is somehow cleaned up, PHI access history must survive.
    CONSTRAINT fk_pal_document FOREIGN KEY (document_id) REFERENCES document (id)
);

COMMENT ON TABLE  phi_access_log IS 'Immutable HIPAA PHI access audit log. Never updated or deleted.';
COMMENT ON COLUMN phi_access_log.document_number      IS 'Denormalized from document for efficient audit queries without joins.';
COMMENT ON COLUMN phi_access_log.document_class_code  IS 'Denormalized from document_class for efficient audit queries without joins.';

-- Indexes optimised for the audit query patterns expected in compliance reviews:
-- "all access by this user", "all access to this document", "all access in this date range"
CREATE INDEX idx_pal_document_id        ON phi_access_log (document_id);
CREATE INDEX idx_pal_accessed_by_user   ON phi_access_log (accessed_by_user_id);
CREATE INDEX idx_pal_accessed_at        ON phi_access_log (accessed_at);
CREATE INDEX idx_pal_document_number    ON phi_access_log (document_number);
