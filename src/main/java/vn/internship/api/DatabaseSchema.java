package vn.internship.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

final class DatabaseSchema {
    private static final String[] DDL = {
        "IF OBJECT_ID('internship_programs', 'U') IS NULL "
                + "CREATE TABLE internship_programs ("
                + "id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, "
                + "name NVARCHAR(200) NOT NULL, "
                + "opens_at_utc DATETIME2 NOT NULL, "
                + "closes_at_utc DATETIME2 NOT NULL, "
                + "CONSTRAINT CK_internship_program_dates CHECK (closes_at_utc > opens_at_utc))",
        "IF OBJECT_ID('accounts', 'U') IS NULL "
                + "CREATE TABLE accounts ("
                + "id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, "
                + "full_name NVARCHAR(160) NOT NULL, "
                + "email NVARCHAR(254) NOT NULL, "
                + "password_hash NVARCHAR(255) NOT NULL, "
                + "email_verified BIT NOT NULL CONSTRAINT DF_accounts_verified DEFAULT 0, "
                + "verification_token_hash CHAR(64) NULL, "
                + "verification_expires_at_utc DATETIME2 NULL, "
                + "verification_window_started_at_utc DATETIME2 NULL, "
                + "verification_sends_in_window INT NOT NULL "
                + "CONSTRAINT DF_accounts_verification_sends DEFAULT 0, "
                + "CONSTRAINT UQ_accounts_email UNIQUE (email))",
        "IF OBJECT_ID('program_document_requirements', 'U') IS NULL "
                + "CREATE TABLE program_document_requirements ("
                + "id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, "
                + "program_id BIGINT NOT NULL, "
                + "document_type NVARCHAR(40) NOT NULL, "
                + "display_name NVARCHAR(120) NOT NULL, "
                + "is_required BIT NOT NULL, "
                + "CONSTRAINT FK_document_requirements_program FOREIGN KEY (program_id) "
                + "REFERENCES internship_programs(id), "
                + "CONSTRAINT UQ_program_document_type UNIQUE (program_id, document_type))",
        "IF OBJECT_ID('internship_applications', 'U') IS NULL "
                + "CREATE TABLE internship_applications ("
                + "id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, "
                + "account_id BIGINT NOT NULL, "
                + "program_id BIGINT NOT NULL, "
                + "status NVARCHAR(20) NOT NULL CONSTRAINT DF_applications_status DEFAULT 'DRAFT', "
                + "phone NVARCHAR(30) NULL, "
                + "school NVARCHAR(200) NULL, "
                + "major NVARCHAR(160) NULL, "
                + "profile_text NVARCHAR(2000) NULL, "
                + "created_at_utc DATETIME2 NOT NULL "
                + "CONSTRAINT DF_applications_created DEFAULT SYSUTCDATETIME(), "
                + "submitted_at_utc DATETIME2 NULL, "
                + "row_version ROWVERSION NOT NULL, "
                + "CONSTRAINT FK_applications_account FOREIGN KEY (account_id) REFERENCES accounts(id), "
                + "CONSTRAINT FK_applications_program FOREIGN KEY (program_id) "
                + "REFERENCES internship_programs(id), "
                + "CONSTRAINT UQ_application_account_program UNIQUE (account_id, program_id), "
                + "CONSTRAINT CK_application_status CHECK (status IN "
                + "('DRAFT', 'SUBMITTED', 'NEEDS_INFO', 'IN_REVIEW')))",
        "IF OBJECT_ID('application_documents', 'U') IS NULL "
                + "CREATE TABLE application_documents ("
                + "id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, "
                + "application_id BIGINT NOT NULL, "
                + "document_type NVARCHAR(40) NOT NULL, "
                + "file_name NVARCHAR(255) NOT NULL, "
                + "content_type NVARCHAR(100) NOT NULL, "
                + "file_bytes VARBINARY(MAX) NOT NULL, "
                + "uploaded_at_utc DATETIME2 NOT NULL "
                + "CONSTRAINT DF_documents_uploaded DEFAULT SYSUTCDATETIME(), "
                + "CONSTRAINT FK_documents_application FOREIGN KEY (application_id) "
                + "REFERENCES internship_applications(id), "
                + "CONSTRAINT UQ_application_document_type UNIQUE (application_id, document_type))",
        "IF OBJECT_ID('application_email_outbox', 'U') IS NULL "
                + "CREATE TABLE application_email_outbox ("
                + "id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, "
                + "application_id BIGINT NOT NULL, "
                + "recipient_email NVARCHAR(254) NOT NULL, "
                + "program_name NVARCHAR(200) NOT NULL, "
                + "created_at_utc DATETIME2 NOT NULL "
                + "CONSTRAINT DF_outbox_created DEFAULT SYSUTCDATETIME(), "
                + "sent_at_utc DATETIME2 NULL, "
                + "claimed_until_utc DATETIME2 NULL, "
                + "attempt_count INT NOT NULL CONSTRAINT DF_outbox_attempts DEFAULT 0, "
                + "CONSTRAINT FK_outbox_application FOREIGN KEY (application_id) "
                + "REFERENCES internship_applications(id), "
                + "CONSTRAINT UQ_outbox_application UNIQUE (application_id))",
        "IF OBJECT_ID('TR_program_document_requirements_lock', 'TR') IS NULL "
                + "EXEC('CREATE TRIGGER TR_program_document_requirements_lock "
                + "ON program_document_requirements AFTER INSERT, UPDATE, DELETE AS "
                + "BEGIN SET NOCOUNT ON; "
                + "IF EXISTS (SELECT 1 FROM internship_applications a "
                + "JOIN (SELECT program_id FROM inserted UNION SELECT program_id FROM deleted) c "
                + "ON c.program_id = a.program_id WHERE a.status <> ''DRAFT'') "
                + "BEGIN;THROW 50001, ''Document requirements cannot change after an application "
                + "is submitted'', 1; END; END')"
    };

    private DatabaseSchema() {
    }

    static void install(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String ddl : DDL) {
                statement.execute(ddl);
            }
        }
    }
}
