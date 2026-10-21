
-- ============================================================================
-- Nguồn hợp nhất: schema.sql
-- ============================================================================


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
-- NHÓM 3: QUẢN LÝ MENTOR & PHÒNG BAN  (tạo trước vì các bảng khác tham chiếu)
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

-- 11. mentors ---------------------------------------------------------------
CREATE TABLE mentors (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT      NULL,
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(254) NOT NULL,
    mentor_code    VARCHAR(30) NOT NULL,
    department_id  BIGINT      NULL,
    job_title      VARCHAR(100) NULL,
    max_capacity   INT         NOT NULL DEFAULT 5,
    status         VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_mentors_user_id UNIQUE (user_id),
    CONSTRAINT uq_mentors_email UNIQUE (email),
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

-- 7.1 Hàng đợi email kết quả xét duyệt -------------------------------------
CREATE TABLE candidate_result_email_queue (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id        BIGINT       NOT NULL,
    recipient_email     VARCHAR(254) NOT NULL,
    recipient_name      VARCHAR(150) NOT NULL,
    desired_position    VARCHAR(150) NULL,
    decision            VARCHAR(20)  NOT NULL,
    decision_reason     TEXT         NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    attempt_count       INT          NOT NULL DEFAULT 0,
    scheduled_at        DATETIME     NOT NULL,
    next_attempt_at     DATETIME     NOT NULL,
    sent_at             DATETIME     NULL,
    last_error          VARCHAR(1000) NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_result_email_candidate UNIQUE (candidate_id),
    CONSTRAINT fk_result_email_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_result_email_decision CHECK (decision IN ('PASSED','FAILED')),
    CONSTRAINT chk_result_email_status CHECK (status IN ('PENDING','PROCESSING','SENT','FAILED','FAILED_FINAL','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Hàng đợi gửi email kết quả xét duyệt';

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
    intern_note          VARCHAR(500) NULL,
    progress_updated_at  DATETIME     NULL,
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
    CONSTRAINT uq_weekly_reports_profile_week UNIQUE (intern_profile_id, week_number),
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

-- Lịch làm việc linh hoạt theo nhóm/phòng ban -------------------------------
CREATE TABLE work_schedules (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id    BIGINT        NOT NULL,
    shift_name  VARCHAR(100)  NOT NULL,
    start_time  TIME          NOT NULL,
    end_time    TIME          NOT NULL,
    work_days   VARCHAR(100)  NOT NULL,
    note        VARCHAR(1000) NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_work_schedules_group UNIQUE (group_id),
    CONSTRAINT fk_work_schedules_department FOREIGN KEY (group_id) REFERENCES departments(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_work_schedule_time CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch làm việc linh hoạt theo nhóm/phòng ban';

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
    status              VARCHAR(30)  NOT NULL DEFAULT 'OPEN',
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
-- (Theo khuyến nghị tại Phần 4 của tài liệu phân tích)
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


-- ============================================================================
-- Nguồn hợp nhất: migration-002-review-email-queue.sql
-- ============================================================================

CREATE TABLE IF NOT EXISTS review_email_queue (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  document_id BIGINT NOT NULL,
  recipient_email VARCHAR(254) NOT NULL,
  recipient_name VARCHAR(150) NOT NULL,
  document_name VARCHAR(255) NOT NULL,
  decision VARCHAR(20) NOT NULL,
  rejection_reason TEXT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  attempt_count INT NOT NULL DEFAULT 0,
  scheduled_at DATETIME NOT NULL,
  next_attempt_at DATETIME NOT NULL,
  sent_at DATETIME NULL,
  last_error VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_review_email_document UNIQUE (document_id),
  CONSTRAINT fk_review_email_document FOREIGN KEY (document_id) REFERENCES intern_documents(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================================
-- Nguồn hợp nhất: migration-004-meeting-email.sql
-- ============================================================================


CREATE TABLE IF NOT EXISTS meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    meeting_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    meeting_mode VARCHAR(20) NOT NULL,
    location_or_link VARCHAR(500) NOT NULL,
    description TEXT NULL,
    creator_user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version_no INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_meetings_creator FOREIGN KEY (creator_user_id) REFERENCES users(id),
    CONSTRAINT chk_meetings_mode CHECK (meeting_mode IN ('ONLINE','OFFLINE')),
    CONSTRAINT chk_meetings_status CHECK (status IN ('ACTIVE','CANCELLED')),
    CONSTRAINT chk_meetings_time CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS meeting_recipients (
    meeting_id BIGINT NOT NULL,
    intern_profile_id BIGINT NOT NULL,
    recipient_name VARCHAR(150) NOT NULL,
    recipient_email VARCHAR(254) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_email_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_email_type VARCHAR(20) NOT NULL DEFAULT 'INVITATION',
    last_email_at DATETIME NULL,
    PRIMARY KEY (meeting_id, intern_profile_id),
    CONSTRAINT fk_meeting_recipients_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    CONSTRAINT fk_meeting_recipients_intern FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS meeting_email_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    intern_profile_id BIGINT NOT NULL,
    meeting_version INT NOT NULL,
    email_type VARCHAR(20) NOT NULL,
    recipient_email VARCHAR(254) NULL,
    recipient_name VARCHAR(150) NOT NULL,
    subject_line VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at DATETIME NULL,
    last_error VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_meeting_email_event UNIQUE (meeting_id,intern_profile_id,meeting_version,email_type),
    CONSTRAINT fk_meeting_email_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    CONSTRAINT fk_meeting_email_intern FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id),
    CONSTRAINT chk_meeting_email_type CHECK (email_type IN ('INVITATION','UPDATE','CANCELLATION')),
    CONSTRAINT chk_meeting_email_status CHECK (status IN ('PENDING','PROCESSING','SENT','FAILED','FAILED_FINAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_meetings_datetime ON meetings(meeting_date,start_time);
CREATE INDEX idx_meeting_email_due ON meeting_email_queue(status,next_attempt_at);


-- ============================================================================
-- Nguồn hợp nhất: migration-005-mentor-final-evaluation.sql
-- ============================================================================

-- Run once on an existing internship_management database before deploying this version.

CREATE TABLE IF NOT EXISTS mentor_final_evaluations (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id        BIGINT       NOT NULL,
    mentor_id                BIGINT       NOT NULL,
    status                   VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    knowledge_score          TINYINT      NULL,
    problem_solving_score    TINYINT      NULL,
    initiative_score         TINYINT      NULL,
    teamwork_score           TINYINT      NULL,
    skill_score              DECIMAL(3,1) NULL,
    attitude_score           DECIMAL(3,1) NULL,
    overall_score            DECIMAL(3,1) NULL,
    overall_comments         VARCHAR(2000) NOT NULL DEFAULT '',
    revision_no              INT          NOT NULL DEFAULT 1,
    finalization_count       INT          NOT NULL DEFAULT 0,
    finalized_at             DATETIME     NULL,
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_mentor_final_evaluation_intern UNIQUE (intern_profile_id),
    CONSTRAINT fk_mentor_final_evaluation_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_mentor_final_evaluation_mentor FOREIGN KEY (mentor_id) REFERENCES mentors(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_mentor_final_evaluation_status CHECK (status IN ('DRAFT','FINALIZED','FINALIZED_AGAIN')),
    CONSTRAINT chk_mentor_final_evaluation_knowledge CHECK (knowledge_score IS NULL OR knowledge_score BETWEEN 1 AND 5),
    CONSTRAINT chk_mentor_final_evaluation_problem CHECK (problem_solving_score IS NULL OR problem_solving_score BETWEEN 1 AND 5),
    CONSTRAINT chk_mentor_final_evaluation_initiative CHECK (initiative_score IS NULL OR initiative_score BETWEEN 1 AND 5),
    CONSTRAINT chk_mentor_final_evaluation_teamwork CHECK (teamwork_score IS NULL OR teamwork_score BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Đánh giá tổng kết theo tiêu chí của mentor, có lưu trạng thái nháp/chốt';

CREATE TABLE IF NOT EXISTS mentor_final_evaluation_history (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    evaluation_id            BIGINT       NOT NULL,
    revision_no              INT          NOT NULL,
    actor_user_id            BIGINT       NULL,
    action_code              VARCHAR(30)  NOT NULL,
    status                   VARCHAR(30)  NOT NULL,
    knowledge_score          TINYINT      NULL,
    problem_solving_score    TINYINT      NULL,
    initiative_score         TINYINT      NULL,
    teamwork_score           TINYINT      NULL,
    skill_score              DECIMAL(3,1) NULL,
    attitude_score           DECIMAL(3,1) NULL,
    overall_score            DECIMAL(3,1) NULL,
    overall_comments         VARCHAR(2000) NOT NULL DEFAULT '',
    archived_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_mentor_final_evaluation_revision UNIQUE (evaluation_id, revision_no),
    CONSTRAINT fk_mentor_final_evaluation_history_evaluation FOREIGN KEY (evaluation_id)
        REFERENCES mentor_final_evaluations(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_mentor_final_evaluation_history_actor FOREIGN KEY (actor_user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử bất biến các phiên bản đánh giá tổng kết';


-- ============================================================================
-- Nguồn hợp nhất: migration-006-final-report.sql
-- ============================================================================

-- Migration 005: Báo cáo cuối kỳ gửi trường / ban lãnh đạo

-- 1. Danh bạ người nhận (trường hoặc ban lãnh đạo)
CREATE TABLE IF NOT EXISTS report_recipients (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_type     VARCHAR(20)  NOT NULL,
    organization_name  VARCHAR(150) NOT NULL,
    contact_name       VARCHAR(100) NOT NULL,
    email              VARCHAR(150) NOT NULL,
    created_by         BIGINT       NULL,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_report_recipients UNIQUE (recipient_type, organization_name, email),
    CONSTRAINT chk_report_recipients_type CHECK (recipient_type IN ('SCHOOL','LEADER')),
    CONSTRAINT fk_report_recipients_user FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh bạ người nhận báo cáo cuối kỳ';

-- 2. Bản chụp báo cáo (bất biến)
CREATE TABLE IF NOT EXISTS report_snapshots (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type    VARCHAR(20)  NOT NULL,
    program_ids    VARCHAR(255) NOT NULL,
    program_names  VARCHAR(500) NOT NULL,
    payload_json   LONGTEXT     NOT NULL,
    created_by     BIGINT       NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_report_snapshots_type CHECK (report_type IN ('LEADER','SCHOOL')),
    CONSTRAINT fk_report_snapshots_user FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bản chụp báo cáo cuối kỳ, không sửa không xóa';

-- Chặn sửa/xóa bản chụp ngay tại database
DROP TRIGGER IF EXISTS trg_report_snapshots_no_update;
DROP TRIGGER IF EXISTS trg_report_snapshots_no_delete;
DELIMITER $$
CREATE TRIGGER trg_report_snapshots_no_update BEFORE UPDATE ON report_snapshots
FOR EACH ROW BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bản chụp báo cáo không được sửa';
END$$
CREATE TRIGGER trg_report_snapshots_no_delete BEFORE DELETE ON report_snapshots
FOR EACH ROW BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bản chụp báo cáo không được xóa';
END$$
DELIMITER ;

CREATE TABLE IF NOT EXISTS report_send_logs (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    snapshot_id      BIGINT       NOT NULL,
    batch_token      VARCHAR(64)  NOT NULL,
    school_name      VARCHAR(150) NULL,
    recipient_email  VARCHAR(150) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    attempts         INT          NOT NULL DEFAULT 0,
    error_message    VARCHAR(500) NULL,
    sent_by          BIGINT       NULL,
    sent_at          DATETIME     NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_report_send_batch UNIQUE (batch_token, recipient_email),
    CONSTRAINT chk_report_send_status CHECK (status IN ('PENDING','SENT','FAILED')),
    CONSTRAINT fk_report_send_snapshot FOREIGN KEY (snapshot_id) REFERENCES report_snapshots(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_report_send_user FOREIGN KEY (sent_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử gửi báo cáo cho từng địa chỉ';

CREATE INDEX idx_report_snapshots_scope ON report_snapshots(report_type, program_ids, created_at);
CREATE INDEX idx_report_send_snapshot   ON report_send_logs(snapshot_id);


-- ============================================================================
-- Nguồn hợp nhất: demo-data.sql
-- ============================================================================

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

INSERT IGNORE INTO departments (id,dept_code,dept_name,is_active) VALUES
    (1,'IT','Công nghệ thông tin',TRUE);

INSERT IGNORE INTO internship_programs
    (id,program_code,program_name,department_id,quota_count,start_date,end_date,status)
VALUES (1,'DEMO-IT-2026','Chương trình thực tập CNTT 2026',1,10,'2026-10-01','2026-12-31','ACTIVE');

-- Seed data cho intern_profiles
INSERT IGNORE INTO intern_profiles (
    id, user_id, intern_code, identity_number, date_of_birth, gender,
    address, university_name, major_name, phone_number, internship_status
) VALUES
    (1, 4, 'TTS001', '001200001234', '2002-05-15', 'Nam', 'Hà Nội', 'ĐH Bách Khoa', 'Công nghệ thông tin', '0912345678', 'ACTIVE');

UPDATE intern_profiles SET program_id=1,start_date='2026-09-01',end_date='2026-10-31'
WHERE id=1 AND program_id IS NULL;

-- Seed data cho intern_documents
INSERT IGNORE INTO intern_documents (
    id, intern_profile_id, document_type, file_name, file_url, file_size_bytes, approval_status, rejection_note, uploaded_at
) VALUES
    (1, 1, 'CV', 'CV_ThucTapSinh_Demo.pdf', '/uploads/demo_cv.pdf', 1048576, 'PENDING', NULL, NOW());
