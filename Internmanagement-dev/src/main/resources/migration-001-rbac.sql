-- Run once on an existing internship_management database before deploying this version.
USE internship_management;

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO permissions (permission_code, permission_name, module_group) VALUES
    ('INTERN_CREATE', 'Tạo hồ sơ thực tập sinh', 'INTERN'),
    ('INTERN_EDIT', 'Chỉnh sửa hồ sơ thực tập sinh', 'INTERN'),
    ('DOCUMENT_REVIEW', 'Duyệt tài liệu thực tập sinh', 'DOCUMENT'),
    ('DOCUMENT_UPLOAD', 'Tải tài liệu thực tập sinh', 'DOCUMENT');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_code = 'ADMIN'
   OR (r.role_code = 'HR' AND p.permission_code IN ('INTERN_CREATE', 'INTERN_EDIT', 'DOCUMENT_REVIEW'))
   OR (r.role_code = 'INTERN' AND p.permission_code = 'DOCUMENT_UPLOAD');
