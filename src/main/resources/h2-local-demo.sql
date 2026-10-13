CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(30) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP NULL,
    create_at DATE NULL,
    update_at DATE NULL,
    c_password BOOLEAN NOT NULL DEFAULT FALSE,
    act_token VARCHAR(255) NULL,
    ex_date_at DATE NULL,
    phone_number VARCHAR(20) NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NULL,
    target_url VARCHAR(255) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code VARCHAR(30) NOT NULL UNIQUE,
    dept_name VARCHAR(150) NOT NULL,
    head_name VARCHAR(100) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS internship_programs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_code VARCHAR(50) NOT NULL UNIQUE,
    program_name VARCHAR(200) NOT NULL,
    department_id BIGINT NULL,
    quota_count INT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    description TEXT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNING'
);

CREATE TABLE IF NOT EXISTS mentors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    mentor_code VARCHAR(30) NOT NULL UNIQUE,
    department_id BIGINT NULL,
    job_title VARCHAR(100) NULL,
    max_capacity INT NOT NULL DEFAULT 5,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(150) NOT NULL,
    module_group VARCHAR(80) NOT NULL DEFAULT 'GENERAL'
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS intern_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    intern_code VARCHAR(30) NOT NULL UNIQUE,
    identity_number VARCHAR(20) NULL,
    date_of_birth DATE NULL,
    gender VARCHAR(10) NULL,
    address VARCHAR(255) NULL,
    university_name VARCHAR(150) NULL,
    major_name VARCHAR(100) NULL,
    program_id BIGINT NULL,
    mentor_id BIGINT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    bank_account_no VARCHAR(30) NULL,
    bank_name VARCHAR(100) NULL,
    phone_number VARCHAR(20) NULL,
    internship_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS intern_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NULL,
    file_url VARCHAR(500) NULL,
    file_size_bytes BIGINT NULL,
    approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_note TEXT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS candidates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    university_name VARCHAR(150) NULL,
    major_name VARCHAR(100) NULL,
    desired_position VARCHAR(100) NULL,
    cv_file_url VARCHAR(255) NULL,
    application_status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    hr_notes TEXT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS weekly_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id BIGINT NOT NULL,
    week_number INT NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    completed_work TEXT NULL,
    key_learnings TEXT NULL,
    obstacles TEXT NULL,
    attachment_url VARCHAR(500) NULL,
    mentor_score DECIMAL(4,2) NULL,
    mentor_feedback TEXT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED'
);

CREATE TABLE IF NOT EXISTS intern_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id BIGINT NOT NULL UNIQUE,
    mentor_id BIGINT NULL,
    technical_score DECIMAL(4,2) NULL,
    attitude_score DECIMAL(4,2) NULL,
    punctuality_score DECIMAL(4,2) NULL,
    final_score DECIMAL(4,2) NULL,
    final_grade VARCHAR(30) NULL,
    recommend_hiring BOOLEAN NOT NULL DEFAULT FALSE,
    overall_comments TEXT NULL,
    hr_approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS evaluation_criteria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    criteria_name VARCHAR(150) NOT NULL,
    weight_percent INT NOT NULL,
    max_score DECIMAL(4,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action_code VARCHAR(50) NOT NULL,
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NULL,
    old_values_json TEXT NULL,
    new_values_json TEXT NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

MERGE INTO roles (id, role_code, role_name, description) KEY(id) VALUES
    (1, 'ADMIN', 'Quản trị viên', 'Quản trị hệ thống'),
    (2, 'HR', 'Phòng nhân sự', 'Quản lý nhân sự và TTS'),
    (3, 'MENTOR', 'Cán bộ Hướng dẫn', 'Hướng dẫn thực tập sinh'),
    (4, 'INTERN', 'Thực tập sinh', 'Sinh viên thực tập');

MERGE INTO users (id, username, password, email, full_name, status, create_at, update_at)
KEY(id) VALUES
    (1, 'admin_demo', '123456', 'admin.demo@ims.local', 'Quản trị viên Demo', 'ACTIVE', CURRENT_DATE, CURRENT_DATE),
    (2, 'hr_demo', '123456', 'hr.demo@ims.local', 'Nhân sự Demo', 'ACTIVE', CURRENT_DATE, CURRENT_DATE),
    (3, 'mentor_demo', '123456', 'mentor.demo@ims.local', 'Mentor Demo', 'ACTIVE', CURRENT_DATE, CURRENT_DATE),
    (4, 'intern_demo', '123456', 'intern.demo@ims.local', 'Thực tập sinh Demo', 'ACTIVE', CURRENT_DATE, CURRENT_DATE);

MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4);

MERGE INTO departments (id, dept_code, dept_name, head_name, is_active) KEY(id) VALUES
    (1, 'IT', 'Công nghệ thông tin', NULL, TRUE);

MERGE INTO internship_programs (id, program_code, program_name, department_id, quota_count, start_date, end_date, description, status) KEY(id) VALUES
    (1, 'DEMO-IT-2026', 'Chương trình thực tập CNTT 2026', 1, 10, DATE '2026-10-01', DATE '2026-12-31', NULL, 'ACTIVE');

MERGE INTO mentors (id, user_id, mentor_code, department_id, job_title, max_capacity, status) KEY(id) VALUES
    (1, 3, 'MENTOR-001', 1, 'Mentor Demo', 5, 'ACTIVE');

MERGE INTO permissions (id, permission_code, permission_name, module_group) KEY(id) VALUES
    (1, 'INTERN_CREATE', 'Tạo hồ sơ thực tập sinh', 'INTERN'),
    (2, 'INTERN_EDIT', 'Chỉnh sửa hồ sơ thực tập sinh', 'INTERN'),
    (3, 'DOCUMENT_REVIEW', 'Duyệt tài liệu thực tập sinh', 'DOCUMENT'),
    (4, 'DOCUMENT_UPLOAD', 'Tải tài liệu thực tập sinh', 'DOCUMENT');

MERGE INTO role_permissions (role_id, permission_id) KEY(role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4),
    (2, 1), (2, 2), (2, 3),
    (4, 4);

MERGE INTO intern_profiles (id, user_id, intern_code, identity_number, date_of_birth, gender, address, university_name, major_name, program_id, mentor_id, start_date, end_date, phone_number, internship_status) KEY(id) VALUES
    (1, 4, 'TTS001', '001200001234', DATE '2002-05-15', 'Nam', 'Hà Nội', 'ĐH Bách Khoa', 'Công nghệ thông tin', 1, 1, DATE '2026-10-01', DATE '2026-12-31', '0912345678', 'ACTIVE');

MERGE INTO evaluation_criteria (id, criteria_name, weight_percent, max_score) KEY(id) VALUES
    (1, 'Kỹ năng chuyên môn', 50, 10),
    (2, 'Thái độ và tinh thần học hỏi', 30, 10),
    (3, 'Tác phong và kỷ luật', 20, 10);

MERGE INTO intern_documents (id, intern_profile_id, document_type, file_name, file_url, file_size_bytes, approval_status, rejection_note, uploaded_at) KEY(id) VALUES
    (1, 1, 'CV', 'CV_ThucTapSinh_Demo.pdf', '/uploads/demo_cv.pdf', 1048576, 'PENDING', NULL, CURRENT_TIMESTAMP);

INSERT INTO notifications (user_id, title, message, target_url, is_read, created_at)
VALUES
    (1, 'Chào mừng', 'Bạn đã đăng nhập vào hệ thống demo.', '/home', FALSE, CURRENT_TIMESTAMP),
    (2, 'Chào mừng', 'Bạn đã đăng nhập vào hệ thống demo.', '/home', FALSE, CURRENT_TIMESTAMP),
    (3, 'Chào mừng', 'Bạn đã đăng nhập vào hệ thống demo.', '/home', FALSE, CURRENT_TIMESTAMP),
    (4, 'Chào mừng', 'Bạn đã đăng nhập vào hệ thống demo.', '/home', FALSE, CURRENT_TIMESTAMP);
