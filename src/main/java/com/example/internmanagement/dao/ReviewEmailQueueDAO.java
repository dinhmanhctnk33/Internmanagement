package com.example.internmanagement.dao;

import com.example.internmanagement.model.ReviewEmailJob;
import com.example.internmanagement.util.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Durable outbox for application-result email. One row is kept per candidate. */
public class ReviewEmailQueueDAO {
    private static final String TABLE = "candidate_result_email_queue";

    public void ensureSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS candidate_result_email_queue (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              candidate_id BIGINT NOT NULL,
              recipient_email VARCHAR(254) NOT NULL,
              recipient_name VARCHAR(150) NOT NULL,
              desired_position VARCHAR(150) NULL,
              decision VARCHAR(20) NOT NULL,
              decision_reason TEXT NOT NULL,
              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
              attempt_count INT NOT NULL DEFAULT 0,
              scheduled_at DATETIME NOT NULL,
              next_attempt_at DATETIME NOT NULL,
              sent_at DATETIME NULL,
              last_error VARCHAR(1000) NULL,
              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
              updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              CONSTRAINT uq_result_email_candidate UNIQUE (candidate_id),
              CONSTRAINT fk_result_email_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) { s.execute(sql); }
    }

    public int enqueue(Connection connection, int candidateId, String decision,
                       String reason, LocalDateTime scheduledAt) throws SQLException {
        String sql = """
            INSERT INTO candidate_result_email_queue
              (candidate_id,recipient_email,recipient_name,desired_position,decision,decision_reason,
               status,attempt_count,scheduled_at,next_attempt_at)
            SELECT id,email,full_name,desired_position,?,?,'PENDING',0,?,?
            FROM candidates WHERE id=? AND email IS NOT NULL AND email<>''
            ON DUPLICATE KEY UPDATE recipient_email=VALUES(recipient_email),
              recipient_name=VALUES(recipient_name),desired_position=VALUES(desired_position),
              decision=VALUES(decision),decision_reason=VALUES(decision_reason),
              status='PENDING',attempt_count=0,scheduled_at=VALUES(scheduled_at),
              next_attempt_at=VALUES(next_attempt_at),sent_at=NULL,last_error=NULL
            """;
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, decision); s.setString(2, reason);
            s.setTimestamp(3, Timestamp.valueOf(scheduledAt)); s.setTimestamp(4, Timestamp.valueOf(scheduledAt));
            s.setInt(5, candidateId); return s.executeUpdate();
        }
    }

    public List<ReviewEmailJob> findAll() throws SQLException {
        return find("SELECT * FROM " + TABLE + " ORDER BY created_at DESC LIMIT 200", 0);
    }

    public List<ReviewEmailJob> findDue(int limit) throws SQLException {
        return find("SELECT * FROM " + TABLE + " WHERE status IN ('PENDING','FAILED') AND next_attempt_at<=NOW() ORDER BY next_attempt_at,id LIMIT ?", limit);
    }

    private List<ReviewEmailJob> find(String sql, int limit) throws SQLException {
        List<ReviewEmailJob> jobs = new ArrayList<>();
        try (Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(sql)) {
            if (limit>0) s.setInt(1,limit);
            try (ResultSet rs=s.executeQuery()) { while(rs.next()) jobs.add(map(rs)); }
        }
        return jobs;
    }

    public boolean claim(long id) throws SQLException {
        try (Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(
                "UPDATE "+TABLE+" SET status='PROCESSING',updated_at=NOW() WHERE id=? AND status IN ('PENDING','FAILED')")) {
            s.setLong(1,id); return s.executeUpdate()==1;
        }
    }
    public void markSent(long id) throws SQLException { update(id,"UPDATE "+TABLE+" SET status='SENT',sent_at=NOW(),last_error=NULL,updated_at=NOW() WHERE id=?"); }
    public void markFailed(long id,int attempts,int maxAttempts,String error,int retryMinutes) throws SQLException {
        String sql="UPDATE "+TABLE+" SET status=?,attempt_count=?,last_error=?,next_attempt_at=DATE_ADD(NOW(),INTERVAL ? MINUTE),updated_at=NOW() WHERE id=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setString(1,attempts>=maxAttempts?"FAILED_FINAL":"FAILED");s.setInt(2,attempts);
            s.setString(3,truncate(error,1000));s.setInt(4,retryMinutes);s.setLong(5,id);s.executeUpdate();
        }
    }
    private void update(long id,String sql)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setLong(1,id);s.executeUpdate();}}
    private ReviewEmailJob map(ResultSet rs)throws SQLException{
        ReviewEmailJob j=new ReviewEmailJob();j.setId(rs.getLong("id"));j.setCandidateId(rs.getInt("candidate_id"));
        j.setRecipientEmail(rs.getString("recipient_email"));j.setRecipientName(rs.getString("recipient_name"));
        j.setDesiredPosition(rs.getString("desired_position"));j.setDecision(rs.getString("decision"));
        j.setDecisionReason(rs.getString("decision_reason"));j.setStatus(rs.getString("status"));
        j.setAttemptCount(rs.getInt("attempt_count"));j.setLastError(rs.getString("last_error"));
        Timestamp t=rs.getTimestamp("scheduled_at");if(t!=null)j.setScheduledAt(t.toLocalDateTime());
        t=rs.getTimestamp("sent_at");if(t!=null)j.setSentAt(t.toLocalDateTime());return j;
    }
    private String truncate(String value,int length){if(value==null)return "Lỗi không xác định";return value.length()<=length?value:value.substring(0,length);}
}
