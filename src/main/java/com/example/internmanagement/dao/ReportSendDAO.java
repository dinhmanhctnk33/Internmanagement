package com.example.internmanagement.dao;

import com.example.internmanagement.model.ReportSendLog;
import com.example.internmanagement.util.DBConnection;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lịch sử gửi báo cáo, từng địa chỉ một. Chống gửi trùng bằng batch_token (mỗi lần xác nhận một mã). */
public class ReportSendDAO {
    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String SELECT = "SELECT l.id, l.snapshot_id, l.batch_token, l.school_name, l.recipient_email, "
            + "l.status, l.attempts, l.error_message, u.full_name, l.sent_at, l.created_at "
            + "FROM report_send_logs l LEFT JOIN users u ON u.id = l.sent_by ";

    /**
     * Ghi trước các dòng PENDING cho từng địa chỉ trong lần xác nhận này.
     * targets: mỗi phần tử là {tên trường (hoặc null nếu gửi lãnh đạo), email}.
     * Địa chỉ nào đã có trong cùng batchToken (bấm gửi hai lần) sẽ bị bỏ qua và KHÔNG nằm trong kết quả.
     */
    public List<ReportSendLog> createBatch(long snapshotId, String batchToken, long userId,
                                           List<String[]> targets, String ip) throws SQLException {
        if (batchToken == null || batchToken.isBlank() || batchToken.length() > 64)
            throw new IllegalArgumentException("Mã xác nhận gửi không hợp lệ.");
        List<ReportSendLog> created = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT IGNORE INTO report_send_logs(snapshot_id, batch_token, school_name, recipient_email, status, sent_by) "
                             + "VALUES(?,?,?,?,'PENDING',?)", Statement.RETURN_GENERATED_KEYS)) {
            for (String[] t : targets) {
                ps.setLong(1, snapshotId);
                ps.setString(2, batchToken);
                ps.setString(3, t[0]);
                ps.setString(4, t[1]);
                ps.setLong(5, userId);
                if (ps.executeUpdate() == 1) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            ReportSendLog log = new ReportSendLog();
                            log.setId(keys.getLong(1));
                            log.setSnapshotId(snapshotId);
                            log.setBatchToken(batchToken);
                            log.setSchoolName(t[0]);
                            log.setRecipientEmail(t[1]);
                            log.setStatus(ReportSendLog.PENDING);
                            created.add(log);
                        }
                    }
                }
            }
            if (!created.isEmpty()) {
                List<String> emails = new ArrayList<>();
                for (ReportSendLog l : created) emails.add(l.getRecipientEmail());
                Map<String, Object> audit = new LinkedHashMap<>();
                audit.put("batch", batchToken);
                audit.put("emails", emails);
                ReportDAO.audit(c, userId, "SEND_REPORT", "report_snapshots", snapshotId, GSON.toJson(audit), ip);
            }
        }
        return created;
    }

    public void markSent(long logId, int attempts) throws SQLException {
        update(logId, ReportSendLog.SENT, attempts, null);
    }

    public void markFailed(long logId, int attempts, String error) throws SQLException {
        String e = error == null ? "Lỗi không xác định" : error;
        update(logId, ReportSendLog.FAILED, attempts, e.length() > 500 ? e.substring(0, 500) : e);
    }

    public List<ReportSendLog> listBySnapshot(long snapshotId) throws SQLException {
        List<ReportSendLog> result = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE l.snapshot_id = ? ORDER BY l.id DESC")) {
            ps.setLong(1, snapshotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public ReportSendLog find(long logId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE l.id = ?")) {
            ps.setLong(1, logId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Tóm tắt tình trạng gửi cho từng bản chụp: key = snapshotId, value có "total", "sent", "failed". */
    public Map<Long, Map<String, Integer>> summaries() throws SQLException {
        Map<Long, Map<String, Integer>> result = new LinkedHashMap<>();
        String sql = "SELECT snapshot_id, COUNT(*) AS total, SUM(status = 'SENT') AS sent, "
                + "SUM(status = 'FAILED') AS failed FROM report_send_logs GROUP BY snapshot_id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Integer> row = new LinkedHashMap<>();
                row.put("total", rs.getInt("total"));
                row.put("sent", rs.getInt("sent"));
                row.put("failed", rs.getInt("failed"));
                result.put(rs.getLong("snapshot_id"), row);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    private void update(long logId, String status, int attempts, String error) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE report_send_logs SET status = ?, attempts = ?, error_message = ?, sent_at = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, attempts);
            ps.setString(3, error);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // giờ máy chủ
            ps.setLong(5, logId);
            ps.executeUpdate();
        }
    }

    private ReportSendLog map(ResultSet rs) throws SQLException {
        ReportSendLog l = new ReportSendLog();
        l.setId(rs.getLong("id"));
        l.setSnapshotId(rs.getLong("snapshot_id"));
        l.setBatchToken(rs.getString("batch_token"));
        l.setSchoolName(rs.getString("school_name"));
        l.setRecipientEmail(rs.getString("recipient_email"));
        l.setStatus(rs.getString("status"));
        l.setAttempts(rs.getInt("attempts"));
        l.setErrorMessage(rs.getString("error_message"));
        l.setSentByName(rs.getString("full_name"));
        Timestamp sent = rs.getTimestamp("sent_at");
        l.setSentAt(sent == null ? null : sent.toLocalDateTime().format(DATETIME));
        l.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().format(DATETIME));
        return l;
    }
}
