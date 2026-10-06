package com.example.internmanagement.dao;

import com.example.internmanagement.model.ReviewEmailJob;
import com.example.internmanagement.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewEmailQueueDAO {
    public void ensureSchema() throws SQLException {
        String sql = """
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        try (Connection connection = DBConnection.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public int enqueue(Connection connection, int documentId, String decision,
                       String rejectionReason, LocalDateTime scheduledAt) throws SQLException {
        String sql = """
            INSERT INTO review_email_queue
              (document_id, recipient_email, recipient_name, document_name, decision,
               rejection_reason, status, attempt_count, scheduled_at, next_attempt_at)
            SELECT d.id, u.email, u.full_name, d.file_name, ?, ?, 'PENDING', 0, ?, ?
            FROM intern_documents d
            JOIN intern_profiles ip ON ip.id = d.intern_profile_id
            JOIN users u ON u.id = ip.user_id
            WHERE d.id = ? AND u.email IS NOT NULL AND u.email <> ''
            ON DUPLICATE KEY UPDATE recipient_email=VALUES(recipient_email),
              recipient_name=VALUES(recipient_name), document_name=VALUES(document_name),
              decision=VALUES(decision), rejection_reason=VALUES(rejection_reason),
              status='PENDING', attempt_count=0, scheduled_at=VALUES(scheduled_at),
              next_attempt_at=VALUES(next_attempt_at), sent_at=NULL, last_error=NULL
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, decision);
            statement.setString(2, rejectionReason);
            statement.setTimestamp(3, Timestamp.valueOf(scheduledAt));
            statement.setTimestamp(4, Timestamp.valueOf(scheduledAt));
            statement.setInt(5, documentId);
            return statement.executeUpdate();
        }
    }

    public List<ReviewEmailJob> findAll() throws SQLException {
        String sql = "SELECT * FROM review_email_queue ORDER BY created_at DESC LIMIT 200";
        List<ReviewEmailJob> jobs = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) jobs.add(map(results));
        }
        return jobs;
    }

    public List<ReviewEmailJob> findDue(int limit) throws SQLException {
        String sql = "SELECT * FROM review_email_queue WHERE status IN ('PENDING','FAILED') " +
                "AND next_attempt_at <= NOW() ORDER BY next_attempt_at,id LIMIT ?";
        List<ReviewEmailJob> jobs = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) jobs.add(map(results));
            }
        }
        return jobs;
    }

    public boolean claim(long id) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "UPDATE review_email_queue SET status='PROCESSING',updated_at=NOW() WHERE id=? AND status IN ('PENDING','FAILED')")) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    public void markSent(long id) throws SQLException {
        update(id, "UPDATE review_email_queue SET status='SENT',sent_at=NOW(),last_error=NULL,updated_at=NOW() WHERE id=?");
    }

    public void markFailed(long id, int attempts, int maxAttempts, String error, int retryMinutes) throws SQLException {
        String status = attempts >= maxAttempts ? "FAILED_FINAL" : "FAILED";
        String sql = "UPDATE review_email_queue SET status=?,attempt_count=?,last_error=?," +
                "next_attempt_at=DATE_ADD(NOW(), INTERVAL ? MINUTE),updated_at=NOW() WHERE id=?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, attempts);
            statement.setString(3, truncate(error, 1000));
            statement.setInt(4, retryMinutes);
            statement.setLong(5, id);
            statement.executeUpdate();
        }
    }

    private void update(long id, String sql) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private ReviewEmailJob map(ResultSet rs) throws SQLException {
        ReviewEmailJob job = new ReviewEmailJob();
        job.setId(rs.getLong("id")); job.setDocumentId(rs.getInt("document_id"));
        job.setRecipientEmail(rs.getString("recipient_email")); job.setRecipientName(rs.getString("recipient_name"));
        job.setDocumentName(rs.getString("document_name")); job.setDecision(rs.getString("decision"));
        job.setRejectionReason(rs.getString("rejection_reason")); job.setStatus(rs.getString("status"));
        job.setAttemptCount(rs.getInt("attempt_count")); job.setLastError(rs.getString("last_error"));
        Timestamp scheduled = rs.getTimestamp("scheduled_at"); if (scheduled != null) job.setScheduledAt(scheduled.toLocalDateTime());
        Timestamp sent = rs.getTimestamp("sent_at"); if (sent != null) job.setSentAt(sent.toLocalDateTime());
        return job;
    }

    private String truncate(String value, int length) {
        if (value == null) return "Lỗi không xác định";
        return value.length() <= length ? value : value.substring(0, length);
    }
}
