-- =========================
-- AGENT REQUESTS (xin làm môi giới)
-- =========================

CREATE TYPE agent_request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

CREATE TABLE agent_requests (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    -- Thông tin môi giới muốn đăng ký
    agency_name VARCHAR(150),
    license_number VARCHAR(100) NOT NULL,
    note TEXT,

    status agent_request_status NOT NULL DEFAULT 'PENDING',
    admin_note TEXT,          -- Ghi chú từ Admin khi duyệt/từ chối

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_agent_req_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_agent_requests_user   ON agent_requests(user_id);
CREATE INDEX idx_agent_requests_status ON agent_requests(status);
