-- Existing databases are also upgraded automatically during application startup.
USE internship_management;

CREATE TABLE IF NOT EXISTS meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    starts_at DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_by_user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meetings_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT chk_meetings_status CHECK (status IN ('SCHEDULED','CANCELLED','COMPLETED'))
);

CREATE TABLE IF NOT EXISTS meeting_attendees (
    meeting_id BIGINT NOT NULL,
    intern_profile_id BIGINT NOT NULL,
    PRIMARY KEY (meeting_id, intern_profile_id),
    CONSTRAINT fk_meeting_attendees_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_meeting_attendees_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE CASCADE
);