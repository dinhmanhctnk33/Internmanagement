-- Migration 005: Báo cáo cuối kỳ gửi trường / ban lãnh đạo
USE internship_management;

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