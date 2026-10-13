-- Existing internship_management databases are upgraded automatically at startup.
USE internship_management;

ALTER TABLE notifications
    ADD COLUMN notification_type VARCHAR(50) NULL,
    ADD COLUMN target_entity_type VARCHAR(50) NULL,
    ADD COLUMN target_entity_id BIGINT NULL,
    ADD COLUMN reminder_key VARCHAR(150) NULL,
    ADD CONSTRAINT uq_notifications_reminder UNIQUE (user_id, reminder_key);