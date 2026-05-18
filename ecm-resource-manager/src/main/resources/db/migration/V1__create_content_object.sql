-- ============================================================
-- V1 — Content Object
-- The only table in the resource manager database.
-- Stores metadata about each binary object uploaded to MinIO.
-- The binary content itself lives in MinIO; this table is the
-- index that makes it findable and auditable.
-- ============================================================

CREATE TABLE content_object (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    -- The key used to address this object in MinIO.
    -- Format: {document_class_lower}/{year}/{month}/{id}.{ext}
    -- Example: medical_claim/2025/05/3f9a1b2c-....pdf
    storage_key         VARCHAR(500) NOT NULL,
    bucket_name         VARCHAR(100) NOT NULL,
    original_file_name  VARCHAR(255),
    content_type        VARCHAR(100),
    file_size_bytes     BIGINT      NOT NULL,
    sha256_checksum     VARCHAR(64) NOT NULL,
    -- Denormalized from the library service call — used to build
    -- the storage key hierarchy and for operational log filtering.
    document_class_code VARCHAR(50),
    uploaded_by_user_id VARCHAR(100),
    -- Soft-delete on disposition: binary is deleted from MinIO,
    -- but this metadata record is preserved for audit purposes.
    is_deleted          BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_content_object PRIMARY KEY (id),
    CONSTRAINT uq_content_object_storage_key UNIQUE (storage_key)
);

COMMENT ON TABLE  content_object                IS 'Metadata index for binary objects stored in MinIO. Records survive content deletion.';
COMMENT ON COLUMN content_object.sha256_checksum IS 'Hex-encoded SHA-256 of the binary content at upload time. Used for integrity verification.';
COMMENT ON COLUMN content_object.is_deleted      IS 'True after disposition. Binary deleted from MinIO; this record retained permanently.';

CREATE INDEX idx_co_document_class ON content_object (document_class_code);
CREATE INDEX idx_co_is_deleted     ON content_object (is_deleted);
