-- ============================================================
-- V3 — Users, Roles, and Permissions
-- Creates: ecm_user, ecm_role, user_role, role_permission
--
-- The permission model is role-per-document-class: each role
-- has a separate permission row for each document class it can
-- access. This allows fine-grained control without complex ACLs.
-- ============================================================

-- ------------------------------------------------------------
-- ecm_role
-- System-defined roles. These are seeded in V12 and are not
-- created by end users. The role codes match RoleCode enum.
-- ------------------------------------------------------------
CREATE TABLE ecm_role (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    code        VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_ecm_role PRIMARY KEY (id),
    CONSTRAINT uq_ecm_role_code UNIQUE (code)
);

COMMENT ON TABLE ecm_role IS 'System-defined roles governing access to document classes and operations';

-- ------------------------------------------------------------
-- ecm_user
-- Authenticated principals. Passwords are stored as bcrypt
-- hashes — never plaintext. Deactivation uses is_active = false;
-- user records are never deleted to preserve audit trail integrity.
-- ------------------------------------------------------------
CREATE TABLE ecm_user (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    username        VARCHAR(100) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(255),
    -- scope_type controls how much of the repository this user can see
    -- within their permitted document classes. Matches ScopeType enum.
    scope_type      VARCHAR(20) NOT NULL DEFAULT 'OWN_QUEUE',
    -- region_code used when scope_type = OWN_REGION
    region_code     VARCHAR(20),
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_ecm_user PRIMARY KEY (id),
    CONSTRAINT uq_ecm_user_username UNIQUE (username)
);

COMMENT ON TABLE  ecm_user            IS 'Authenticated users. Never deleted — deactivate with is_active = false.';
COMMENT ON COLUMN ecm_user.scope_type IS 'OWN_QUEUE | OWN_REGION | ALL — limits visible documents within permitted classes';

-- ------------------------------------------------------------
-- user_role
-- Assigns roles to users. Supports effective dates so role
-- assignments can be scheduled in advance or expire automatically.
-- A user may hold multiple roles.
-- ------------------------------------------------------------
CREATE TABLE user_role (
    id              UUID    NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID    NOT NULL,
    role_id         UUID    NOT NULL,
    effective_from  DATE    NOT NULL DEFAULT CURRENT_DATE,
    effective_to    DATE,               -- null means no expiry
    assigned_by_user_id VARCHAR(100),   -- user ID of the SYSTEM_ADMIN who made this assignment
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_role PRIMARY KEY (id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES ecm_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES ecm_role (id),
    CONSTRAINT uq_user_role_active UNIQUE (user_id, role_id, effective_from)
);

COMMENT ON TABLE  user_role              IS 'Role assignments with effective date window. Multiple roles per user supported.';
COMMENT ON COLUMN user_role.effective_to IS 'Null = no expiry. Set to past date to revoke without deleting the record.';

CREATE INDEX idx_user_role_user_id ON user_role (user_id);
CREATE INDEX idx_user_role_role_id ON user_role (role_id);

-- ------------------------------------------------------------
-- role_permission
-- The permission matrix: what each role can do per document class.
-- One row per role-document_class pair. Missing row = no access.
-- ------------------------------------------------------------
CREATE TABLE role_permission (
    id                  UUID    NOT NULL DEFAULT gen_random_uuid(),
    role_id             UUID    NOT NULL,
    -- document_class_code stored as string; simpler than FK for this lookup pattern
    document_class_code VARCHAR(50) NOT NULL,
    can_read            BOOLEAN NOT NULL DEFAULT FALSE,
    can_create          BOOLEAN NOT NULL DEFAULT FALSE,
    can_transition      BOOLEAN NOT NULL DEFAULT FALSE,
    can_dispose         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_role_permission PRIMARY KEY (id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES ecm_role (id),
    CONSTRAINT uq_role_permission_pair UNIQUE (role_id, document_class_code)
);

COMMENT ON TABLE  role_permission                IS 'Permission matrix: what each role may do per document class.';
COMMENT ON COLUMN role_permission.can_dispose    IS 'Separate flag because disposition is a one-way destructive action requiring explicit grant.';
COMMENT ON COLUMN role_permission.can_transition IS 'Grants the ability to trigger lifecycle state transitions; specific from/to pairs further constrained by state machine guards.';

CREATE INDEX idx_role_permission_role_id ON role_permission (role_id);
