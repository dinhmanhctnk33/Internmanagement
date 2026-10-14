-- SCRUM-153: Hồ sơ mentor do HR tạo, tách khỏi tài khoản đăng nhập.
-- Chạy 1 lần trên database internship_management (sau migration-004).
USE internship_management;

-- Tài khoản bị xóa -> hồ sơ về "Chưa liên kết" (thay vì chặn xóa như trước)
ALTER TABLE mentors DROP FOREIGN KEY fk_mentors_user;

ALTER TABLE mentors
    MODIFY user_id BIGINT NULL,                                   -- NULL = chưa liên kết tài khoản
    ADD COLUMN full_name  VARCHAR(100) NULL AFTER user_id,
    ADD COLUMN email      VARCHAR(100) NULL AFTER full_name,
    ADD COLUMN version    INT NOT NULL DEFAULT 0,                 -- khóa lạc quan: 2 HR cùng sửa
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Mentor cũ: chép tên/email từ tài khoản sang hồ sơ
UPDATE mentors m JOIN users u ON u.id = m.user_id
SET m.full_name = u.full_name, m.email = u.email;

ALTER TABLE mentors
    MODIFY full_name VARCHAR(100) NOT NULL,
    MODIFY email     VARCHAR(100) NOT NULL,
    ADD CONSTRAINT uq_mentors_email UNIQUE (email),
    ADD CONSTRAINT fk_mentors_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE;