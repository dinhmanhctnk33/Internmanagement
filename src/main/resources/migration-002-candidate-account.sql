ALTER TABLE candidates
    ADD COLUMN user_id BIGINT NULL,
    ADD CONSTRAINT uq_candidates_user_id UNIQUE (user_id),
    ADD CONSTRAINT fk_candidates_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE;

INSERT IGNORE INTO roles (role_code, role_name, description)
VALUES ('CANDIDATE', 'Ứng viên', 'Ứng viên đăng ký chương trình thực tập');
