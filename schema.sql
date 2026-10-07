CREATE DATABASE IF NOT EXISTS internship_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE internship_management;

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- NHÓM 1: QUẢN TRỊ HỆ THỐNG & PHÂN QUYỀN
-- =====================================================================

-- 1. users -------------------------------------------------------------
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL,
    password        VARCHAR(255) NOT NULL,
    email           VARCHAR(100) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    last_login_at   DATETIME     NULL,
    create_at       DATE         NULL,
    update_at       DATE         NULL,
    c_password      BOOLEAN      NOT NULL DEFAULT FALSE,
    act_token       VARCHAR(255) NULL,
    ex_date_at      DATE         NULL,
    phone_number    VARCHAR(20)  NULL,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','INACTIVE','LOCKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tài khoản đăng nhập của tất cả người dùng (Admin, HR, Mentor, TTS)';

-- 2. roles ---------------------------------------------------------------
CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code   VARCHAR(30)  NOT NULL,
    role_name   VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    CONSTRAINT uq_roles_role_code UNIQUE (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh mục vai trò (ADMIN, HR_MANAGER, MENTOR, INTERN, CANDIDATE)';

-- 3. permissions -----------------------------------------------------------
CREATE TABLE permissions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code  VARCHAR(50)  NOT NULL,
    permission_name  VARCHAR(100) NOT NULL,
    module_group     VARCHAR(50)  NULL,
    CONSTRAINT uq_permissions_code UNIQUE (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh mục quyền thao tác chi tiết';

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Liên kết quyền theo vai trò';

-- 4. user_roles (bảng trung gian N-M) ---------------------------------------
CREATE TABLE user_roles (
    user_id      BIGINT   NOT NULL,
    role_id      BIGINT   NOT NULL,
    assigned_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Liên kết N-M giữa User và Role';

-- =====================================================================
-- NHÓM 2: TIẾP NHẬN VÀ XÉT DUYỆT
-- =====================================================================

-- 5. candidates ---------------------------------------------------------
CREATE TABLE candidates (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(100) NOT NULL,
    phone               VARCHAR(20)  NULL,
    university_name     VARCHAR(150) NULL,
    major_name          VARCHAR(100) NULL,
    desired_position    VARCHAR(100) NULL,
    cv_file_url         VARCHAR(255) NULL,
    application_status  VARCHAR(30)  NOT NULL DEFAULT 'NEW',
    hr_notes            TEXT         NULL,
    applied_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_candidates_status CHECK (application_status IN ('NEW','INTERVIEWING','ACCEPTED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Hồ sơ ứng viên đăng ký thực tập trực tuyến';

-- =====================================================================
-- NHÓM 3: QUẢN LÝ MENTOR & PHÒNG BAN
-- =====================================================================

-- 9. departments ----------------------------------------------------------
CREATE TABLE departments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code  VARCHAR(30)  NOT NULL,
    dept_name  VARCHAR(150) NOT NULL,
    head_name  VARCHAR(100) NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_departments_code UNIQUE (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh mục phòng ban tiếp nhận thực tập sinh';

-- 10. internship_programs --------------------------------------------------
CREATE TABLE internship_programs (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_code   VARCHAR(50)  NOT NULL,
    program_name   VARCHAR(200) NOT NULL,
    department_id  BIGINT       NULL,
    quota_count    INT          NULL,
    start_date     DATE         NULL,
    end_date       DATE         NULL,
    description    TEXT         NULL,
    status         VARCHAR(30)  NOT NULL DEFAULT 'PLANNING',
    CONSTRAINT uq_programs_code UNIQUE (program_code),
    CONSTRAINT fk_programs_department FOREIGN KEY (department_id) REFERENCES departments(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_programs_status CHECK (status IN ('PLANNING','ACTIVE','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Các đợt/chương trình thực tập';

CREATE TABLE IF NOT EXISTS internship_applications (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    applicant_user_id   BIGINT       NOT NULL,
    program_id          BIGINT       NOT NULL,
    university_name     VARCHAR(150) NULL,
    major_name          VARCHAR(100) NULL,
    study_year          INT          NULL,
    gpa                 DECIMAL(4,2) NULL,
    cover_letter        TEXT         NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    submitted_at        DATETIME     NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_applications_applicant_program UNIQUE (applicant_user_id, program_id),
    CONSTRAINT fk_applications_user FOREIGN KEY (applicant_user_id) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_applications_program FOREIGN KEY (program_id) REFERENCES internship_programs(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_applications_status CHECK (status IN ('DRAFT','SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED')),
    CONSTRAINT chk_applications_gpa CHECK (gpa IS NULL OR gpa BETWEEN 0 AND 4),
    CONSTRAINT chk_applications_study_year CHECK (study_year IS NULL OR study_year BETWEEN 1 AND 8)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Hồ sơ ứng tuyển; mỗi người có một bản ghi theo từng chương trình';

CREATE TABLE IF NOT EXISTS application_decision_history (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id      BIGINT       NOT NULL,
    reviewer_user_id    BIGINT       NULL,
    previous_status     VARCHAR(20)  NOT NULL,
    decision_status     VARCHAR(20)  NOT NULL,
    reason              TEXT         NULL,
    decided_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_history_application FOREIGN KEY (application_id) REFERENCES internship_applications(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_application_history_reviewer FOREIGN KEY (reviewer_user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_application_history_decision CHECK (decision_status IN ('APPROVED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử bất biến các quyết định xét duyệt hồ sơ';

-- 11. mentors ---------------------------------------------------------------
CREATE TABLE mentors (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT      NOT NULL,
    mentor_code    VARCHAR(30) NOT NULL,
    department_id  BIGINT      NULL,
    job_title      VARCHAR(100) NULL,
    max_capacity   INT         NOT NULL DEFAULT 5,
    status         VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_mentors_user_id UNIQUE (user_id),
    CONSTRAINT uq_mentors_code UNIQUE (mentor_code),
    CONSTRAINT fk_mentors_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_mentors_department FOREIGN KEY (department_id) REFERENCES departments(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Hồ sơ người hướng dẫn thực tập (liên kết 1-1 với users)';

-- 12. program_milestones -----------------------------------------------------
CREATE TABLE program_milestones (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_id       BIGINT       NOT NULL,
    milestone_title  VARCHAR(150) NOT NULL,
    target_date      DATE         NULL,
    description      TEXT         NULL,
    CONSTRAINT fk_milestones_program FOREIGN KEY (program_id) REFERENCES internship_programs(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Các mốc thời gian quan trọng của chương trình thực tập';

-- =====================================================================
-- NHÓM 4: QUẢN LÝ HỒ SƠ THỰC TẬP SINH
-- =====================================================================

-- 6. intern_profiles --------------------------------------------------------
CREATE TABLE intern_profiles (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    intern_code        VARCHAR(30)  NOT NULL,
    identity_number    VARCHAR(20)  NULL,
    date_of_birth      DATE         NULL,
    gender             VARCHAR(10)  NULL,
    address            VARCHAR(255) NULL,
    university_name    VARCHAR(150) NULL,
    major_name         VARCHAR(100) NULL,
    program_id         BIGINT       NULL,
    mentor_id          BIGINT       NULL,
    start_date         DATE         NULL,
    end_date           DATE         NULL,
    bank_account_no    VARCHAR(30)  NULL,
    bank_name          VARCHAR(100) NULL,
    phone_number       VARCHAR(20)  NULL,
    internship_status  VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_intern_profiles_user_id UNIQUE (user_id),
    CONSTRAINT uq_intern_profiles_code UNIQUE (intern_code),
    CONSTRAINT uq_intern_profiles_identity UNIQUE (identity_number),
    CONSTRAINT fk_intern_profiles_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_intern_profiles_program FOREIGN KEY (program_id) REFERENCES internship_programs(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_intern_profiles_mentor FOREIGN KEY (mentor_id) REFERENCES mentors(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_intern_profiles_status CHECK (internship_status IN ('ACTIVE','COMPLETED','PAUSED','DROPPED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Hồ sơ chi tiết của Thực tập sinh chính thức (liên kết 1-1 với users)';

-- 7. intern_documents ---------------------------------------------------------
CREATE TABLE intern_documents (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id   BIGINT       NOT NULL,
    document_type       VARCHAR(50)  NOT NULL,
    file_name           VARCHAR(255) NULL,
    file_url            VARCHAR(500) NULL,
    file_size_bytes     BIGINT       NULL,
    approval_status     VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    rejection_note      TEXT         NULL,
    uploaded_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_intern_documents_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_intern_documents_status CHECK (approval_status IN ('PENDING','APPROVED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tài liệu/giấy tờ đính kèm của thực tập sinh';

-- 8. contracts -----------------------------------------------------------------
CREATE TABLE contracts (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_number     VARCHAR(50)    NOT NULL,
    intern_profile_id   BIGINT         NOT NULL,
    effective_date      DATE           NULL,
    expiration_date     DATE           NULL,
    monthly_allowance   DECIMAL(12,2)  NULL,
    contract_pdf_url    VARCHAR(500)   NULL,
    signature_status    VARCHAR(30)    NOT NULL DEFAULT 'DRAFT',
    signed_at           DATETIME       NULL,
    signed_ip_address   VARCHAR(45)    NULL,
    CONSTRAINT uq_contracts_number UNIQUE (contract_number),
    CONSTRAINT fk_contracts_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_contracts_status CHECK (signature_status IN ('DRAFT','SENT','SIGNED','EXPIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Hợp đồng thực tập điện tử';

-- =====================================================================
-- NHÓM 5: QUẢN LÝ CÔNG VIỆC & ĐÁNH GIÁ
-- =====================================================================

-- 13. tasks -----------------------------------------------------------------
CREATE TABLE tasks (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_title           VARCHAR(200) NOT NULL,
    description          TEXT         NULL,
    mentor_id            BIGINT       NULL,
    intern_profile_id    BIGINT       NULL,
    due_date             DATETIME     NULL,
    priority             VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    completion_percent   INT          NOT NULL DEFAULT 0,
    status               VARCHAR(30)  NOT NULL DEFAULT 'TODO',
    deliverable_url      VARCHAR(500) NULL,
    CONSTRAINT fk_tasks_mentor FOREIGN KEY (mentor_id) REFERENCES mentors(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_tasks_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_tasks_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    CONSTRAINT chk_tasks_status CHECK (status IN ('TODO','IN_PROGRESS','COMPLETED','VERIFIED')),
    CONSTRAINT chk_tasks_percent CHECK (completion_percent BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhiệm vụ Mentor giao cho Thực tập sinh';

-- 14. weekly_reports ------------------------------------------------------------
CREATE TABLE weekly_reports (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id   BIGINT        NOT NULL,
    week_number         INT           NOT NULL,
    start_date          DATE          NULL,
    end_date            DATE          NULL,
    completed_work      TEXT          NULL,
    key_learnings       TEXT          NULL,
    obstacles           TEXT          NULL,
    attachment_url      VARCHAR(500)  NULL,
    mentor_score        DECIMAL(4,2)  NULL,
    mentor_feedback     TEXT          NULL,
    status              VARCHAR(30)   NOT NULL DEFAULT 'SUBMITTED',
    CONSTRAINT fk_weekly_reports_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_weekly_reports_status CHECK (status IN ('SUBMITTED','REJECTED','APPROVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Báo cáo tiến độ tuần của Thực tập sinh';

-- 15. evaluation_criteria ---------------------------------------------------------
CREATE TABLE evaluation_criteria (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    criteria_name   VARCHAR(150)  NOT NULL,
    weight_percent  INT           NOT NULL,
    max_score       DECIMAL(4,2)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tiêu chí dùng để chấm điểm kết quả thực tập';

-- 16. intern_evaluations -------------------------------------------------------------
CREATE TABLE intern_evaluations (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id    BIGINT        NOT NULL,
    mentor_id            BIGINT        NULL,
    technical_score      DECIMAL(4,2)  NULL,
    attitude_score       DECIMAL(4,2)  NULL,
    punctuality_score    DECIMAL(4,2)  NULL,
    final_score          DECIMAL(4,2)  NULL,
    final_grade          VARCHAR(30)   NULL,
    recommend_hiring     BOOLEAN       NOT NULL DEFAULT FALSE,
    overall_comments     TEXT          NULL,
    hr_approval_status   VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_evaluations_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_evaluations_mentor FOREIGN KEY (mentor_id) REFERENCES mentors(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_evaluations_grade CHECK (final_grade IN ('EXCELLENT','GOOD','PASS','FAIL')),
    CONSTRAINT chk_evaluations_hr_status CHECK (hr_approval_status IN ('PENDING','APPROVED','PUBLISHED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bảng tổng hợp điểm đánh giá kết quả thực tập sinh cuối kỳ';

-- =====================================================================
-- NHÓM 6: QUẢN LÝ CHẤM CÔNG & THỜI GIAN
-- =====================================================================

-- 17. shift_configs -------------------------------------------------------------
CREATE TABLE shift_configs (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_name             VARCHAR(100) NOT NULL,
    start_time             TIME         NOT NULL,
    end_time               TIME         NOT NULL,
    grace_period_minutes   INT          NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Cấu hình ca làm việc và quy tắc tính trễ';

-- 18. attendance_records -------------------------------------------------------------
CREATE TABLE attendance_records (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id   BIGINT      NOT NULL,
    work_date           DATE        NOT NULL,
    check_in_time       DATETIME    NULL,
    check_out_time      DATETIME    NULL,
    ip_address          VARCHAR(45) NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'ON_TIME',
    CONSTRAINT uq_attendance_profile_date UNIQUE (intern_profile_id, work_date),
    CONSTRAINT fk_attendance_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_attendance_status CHECK (status IN ('ON_TIME','LATE','EARLY_LEAVE','ABSENT','LEAVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhật ký check-in/check-out hằng ngày của Thực tập sinh';

-- 19. leave_requests -------------------------------------------------------------
CREATE TABLE leave_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id   BIGINT        NOT NULL,
    leave_type          VARCHAR(50)   NOT NULL,
    start_date          DATE          NOT NULL,
    end_date            DATE          NOT NULL,
    total_days          DECIMAL(3,1)  NOT NULL,
    reason              TEXT          NULL,
    attachment_url      VARCHAR(500)  NULL,
    approval_status     VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    approver_notes      TEXT          NULL,
    CONSTRAINT fk_leave_requests_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_leave_type CHECK (leave_type IN ('SICK_LEAVE','PERSONAL_LEAVE','UNPAID_LEAVE')),
    CONSTRAINT chk_leave_status CHECK (approval_status IN ('PENDING','APPROVED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Đơn xin nghỉ phép của Thực tập sinh';

-- =====================================================================
-- NHÓM 7: QUẢN LÝ HỖ TRỢ & QUYỀN LỢI
-- =====================================================================

-- 20. allowance_payouts -------------------------------------------------------------
CREATE TABLE allowance_payouts (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id   BIGINT        NOT NULL,
    pay_period          VARCHAR(7)    NOT NULL,
    actual_work_days    INT           NULL,
    base_allowance      DECIMAL(12,2) NULL,
    bonus_allowance     DECIMAL(12,2) NULL DEFAULT 0,
    net_amount          DECIMAL(12,2) NULL,
    payment_status      VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    paid_at             DATETIME      NULL,
    CONSTRAINT uq_allowance_profile_period UNIQUE (intern_profile_id, pay_period),
    CONSTRAINT fk_allowance_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_allowance_status CHECK (payment_status IN ('PENDING','PROCESSING','PAID','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu tính và thanh toán phụ cấp thực tập hằng tháng';

-- 21. support_tickets -------------------------------------------------------------
CREATE TABLE support_tickets (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_code         VARCHAR(30)  NOT NULL,
    intern_profile_id   BIGINT       NOT NULL,
    ticket_category     VARCHAR(50)  NOT NULL,
    subject             VARCHAR(200) NOT NULL,
    description         TEXT         NULL,
    hr_response         TEXT         NULL,
    status              VARCHAR(30)   NOT NULL DEFAULT 'OPEN',
    CONSTRAINT uq_support_tickets_code UNIQUE (ticket_code),
    CONSTRAINT fk_support_tickets_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_support_tickets_status CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Yêu cầu hỗ trợ (giấy xác nhận, thẻ ra vào, thắc mắc) từ TTS tới HR';

-- =====================================================================
-- NHÓM 8: TÍCH HỢP & THÔNG BÁO
-- =====================================================================

-- 22. notifications -------------------------------------------------------------
CREATE TABLE notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     TEXT         NULL,
    target_url  VARCHAR(255) NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Thông báo hệ thống gửi tới người dùng';

-- 23. notification_templates -------------------------------------------------------------
CREATE TABLE notification_templates (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_key        VARCHAR(50)  NOT NULL,
    subject_pattern     VARCHAR(255) NULL,
    body_html_pattern   TEXT         NULL,
    CONSTRAINT uq_notification_templates_key UNIQUE (template_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mẫu nội dung Email/Thông báo tự động';

-- 24. timekeeping_devices -------------------------------------------------------------
CREATE TABLE timekeeping_devices (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code  VARCHAR(50)  NOT NULL,
    device_name  VARCHAR(100) NOT NULL,
    ip_address   VARCHAR(45)  NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'OFFLINE',
    CONSTRAINT uq_timekeeping_devices_code UNIQUE (device_code),
    CONSTRAINT chk_timekeeping_devices_status CHECK (status IN ('ONLINE','OFFLINE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh mục máy chấm công vân tay/QR tại văn phòng';

-- =====================================================================
-- NHÓM 9: QUẢN TRỊ HỆ THỐNG (AUDIT)
-- =====================================================================

-- 25. audit_logs -------------------------------------------------------------
CREATE TABLE audit_logs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NULL,
    action_code       VARCHAR(50)  NOT NULL,
    table_name        VARCHAR(50)  NOT NULL,
    record_id         BIGINT       NULL,
    old_values_json   JSON         NULL,
    new_values_json   JSON         NULL,
    ip_address        VARCHAR(45)  NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhật ký mọi thao tác thay đổi dữ liệu quan trọng, phục vụ truy vết an ninh';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- INDEX BỔ SUNG CHO CÁC TRƯỜNG TRA CỨU TẦN SUẤT CAO
-- =====================================================================
CREATE INDEX idx_users_username            ON users(username);
CREATE INDEX idx_intern_profiles_code      ON intern_profiles(intern_code);
CREATE INDEX idx_attendance_work_date      ON attendance_records(work_date);
CREATE INDEX idx_tasks_status              ON tasks(status);

-- Index hỗ trợ các truy vấn khóa ngoại thường dùng
CREATE INDEX idx_intern_profiles_program   ON intern_profiles(program_id);
CREATE INDEX idx_intern_profiles_mentor    ON intern_profiles(mentor_id);
CREATE INDEX idx_tasks_mentor              ON tasks(mentor_id);
CREATE INDEX idx_tasks_intern_profile      ON tasks(intern_profile_id);
CREATE INDEX idx_weekly_reports_profile    ON weekly_reports(intern_profile_id);
CREATE INDEX idx_leave_requests_profile    ON leave_requests(intern_profile_id);
CREATE INDEX idx_notifications_user        ON notifications(user_id);
CREATE INDEX idx_audit_logs_user           ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_table_record   ON audit_logs(table_name, record_id);
