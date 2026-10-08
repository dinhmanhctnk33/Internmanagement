-- Seed data cho bảng roles
INSERT IGNORE INTO roles (id, role_code, role_name, description) VALUES
    (1, 'ADMIN',  'Quản trị viên', 'Quản trị hệ thống'),
    (2, 'HR',     'Phòng nhân sự', 'Quản lý nhân sự và TTS'),
    (3, 'MENTOR', 'Cán bộ Hướng dẫn', 'Hướng dẫn thực tập sinh'),
    (4, 'INTERN', 'Thực tập sinh', 'Sinh viên thực tập'),
    (5, 'CANDIDATE', 'Ứng viên', 'Ứng viên đăng ký thực tập trực tuyến');

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

-- CHỈ DÙNG CHO MÔI TRƯỜNG DEMO/LOCAL.
INSERT IGNORE INTO users (
    id,
    username, 
    password, 
    email, 
    full_name, 
    status, 
    create_at, 
    update_at
)
VALUES
    (1, 'admin_demo',  '123456', 'admin.demo@ims.local',  'Quản trị viên Demo', 'ACTIVE', CURDATE(), CURDATE()),
    (2, 'hr_demo',     '123456', 'hr.demo@ims.local',     'Nhân sự Demo',       'ACTIVE', CURDATE(), CURDATE()),
    (3, 'mentor_demo', '123456', 'mentor.demo@ims.local', 'Mentor Demo',        'ACTIVE', CURDATE(), CURDATE()),
    (4, 'intern_demo', '123456', 'intern.demo@ims.local', 'Thực tập sinh Demo', 'ACTIVE', CURDATE(), CURDATE());

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r
WHERE (u.username = 'admin_demo'  AND r.role_code = 'ADMIN')
   OR (u.username = 'hr_demo'     AND r.role_code = 'HR')
   OR (u.username = 'mentor_demo' AND r.role_code = 'MENTOR')
   OR (u.username = 'intern_demo' AND r.role_code = 'INTERN');

-- Seed data cho intern_profiles
INSERT IGNORE INTO intern_profiles (
    id, user_id, intern_code, identity_number, date_of_birth, gender, 
    address, university_name, major_name, phone_number, internship_status
) VALUES 
    (1, 4, 'TTS001', '001200001234', '2002-05-15', 'Nam', 'Hà Nội', 'ĐH Bách Khoa', 'Công nghệ thông tin', '0912345678', 'ACTIVE');

-- Seed data cho intern_documents
INSERT IGNORE INTO intern_documents (
    id, intern_profile_id, document_type, file_name, file_url, file_size_bytes, approval_status, rejection_note, uploaded_at
) VALUES
    (1, 1, 'CV', 'CV_ThucTapSinh_Demo.pdf', '/uploads/demo_cv.pdf', 1048576, 'PENDING', NULL, NOW());
