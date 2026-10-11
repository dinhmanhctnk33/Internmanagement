USE internship_management;

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

