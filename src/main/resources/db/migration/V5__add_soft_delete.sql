-- =========================
-- V5: Soft-delete cho các bảng cần thiết
-- =========================

-- ── users ─────────────────────────────────────────────────
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_users_is_deleted ON users(is_deleted);

-- ── owners ────────────────────────────────────────────────
ALTER TABLE owners
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_owners_is_deleted ON owners(is_deleted);

-- ── agents ────────────────────────────────────────────────
ALTER TABLE agents
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_agents_is_deleted ON agents(is_deleted);

-- ── agent_requests ────────────────────────────────────────
ALTER TABLE agent_requests
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_agent_requests_deleted ON agent_requests(is_deleted);

-- ── properties ────────────────────────────────────────────
-- (đã có status + visibility, thêm is_deleted riêng để xóa mềm thực sự
--  tránh lẫn với logic PENDING/APPROVED/HIDDEN)
ALTER TABLE properties
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_properties_is_deleted ON properties(is_deleted);

-- ── viewings ──────────────────────────────────────────────
-- (đã có status CANCELLED nhưng is_deleted để Admin ẩn hoàn toàn record)
ALTER TABLE viewings
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_viewings_is_deleted ON viewings(is_deleted);
