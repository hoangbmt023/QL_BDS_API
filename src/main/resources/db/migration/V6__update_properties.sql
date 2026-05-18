-- =========================
-- V6: Bổ sung các trường cho bảng properties
--     và cập nhật enum property_status
-- =========================

-- ── Thêm trường rejection_reason vào properties ────────────────────────────
ALTER TABLE properties
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

-- ── Cập nhật enum property_status: thêm các giá trị mới ───────────────────
-- PostgreSQL yêu cầu ALTER TYPE ... ADD VALUE để mở rộng enum
ALTER TYPE property_status ADD VALUE IF NOT EXISTS 'SOLD';
ALTER TYPE property_status ADD VALUE IF NOT EXISTS 'RENTED';
ALTER TYPE property_status ADD VALUE IF NOT EXISTS 'DELETED';
