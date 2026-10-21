package com.example.internmanagement.dao;

import com.example.internmanagement.util.DBConnection;

import java.sql.*;
import java.util.*;

public class MentorReportDAO {
    public Long findMentorIdByUserId(long userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id FROM mentors WHERE user_id=? AND status='ACTIVE'")) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? result.getLong(1) : null; }
        }
    }

    public Map<String, Object> summary(long mentorId) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM intern_profiles WHERE mentor_id=? AND internship_status='ACTIVE') intern_count," +
                "(SELECT COUNT(*) FROM weekly_reports wr JOIN intern_profiles ip ON ip.id=wr.intern_profile_id WHERE ip.mentor_id=? AND wr.status='SUBMITTED') pending_count," +
                "(SELECT COUNT(*) FROM weekly_reports wr JOIN intern_profiles ip ON ip.id=wr.intern_profile_id WHERE ip.mentor_id=? AND wr.status='APPROVED') approved_count," +
                "(SELECT ROUND(AVG(wr.mentor_score),1) FROM weekly_reports wr JOIN intern_profiles ip ON ip.id=wr.intern_profile_id WHERE ip.mentor_id=? AND wr.mentor_score IS NOT NULL) average_score";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 4; index++) statement.setLong(index, mentorId);
            try (ResultSet result = statement.executeQuery()) { result.next(); return row(result); }
        }
    }

    public List<Map<String, Object>> interns(long mentorId) throws SQLException {
        String sql = "SELECT ip.id,ip.intern_code,u.full_name,u.email,p.program_name,d.dept_name," +
                "COUNT(wr.id) report_count,SUM(wr.status='SUBMITTED') pending_count,MAX(wr.week_number) latest_week " +
                "FROM intern_profiles ip JOIN users u ON u.id=ip.user_id " +
                "LEFT JOIN internship_programs p ON p.id=ip.program_id LEFT JOIN departments d ON d.id=p.department_id " +
                "LEFT JOIN weekly_reports wr ON wr.intern_profile_id=ip.id " +
                "WHERE ip.mentor_id=? AND ip.internship_status='ACTIVE' " +
                "GROUP BY ip.id,ip.intern_code,u.full_name,u.email,p.program_name,d.dept_name ORDER BY u.full_name";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mentorId); return rows(statement);
        }
    }

    public List<Map<String, Object>> reports(long mentorId, Long internId, String status) throws SQLException {
        String sql = "SELECT wr.id,wr.intern_profile_id,wr.week_number,wr.start_date,wr.end_date,wr.completed_work," +
                "wr.key_learnings,wr.obstacles,wr.attachment_url,wr.mentor_score,wr.mentor_feedback,wr.status," +
                "ip.intern_code,u.full_name,u.email,p.program_name " +
                "FROM weekly_reports wr JOIN intern_profiles ip ON ip.id=wr.intern_profile_id " +
                "JOIN users u ON u.id=ip.user_id LEFT JOIN internship_programs p ON p.id=ip.program_id " +
                "WHERE ip.mentor_id=? AND (? IS NULL OR ip.id=?) AND (?='ALL' OR wr.status=?) " +
                "ORDER BY CASE wr.status WHEN 'SUBMITTED' THEN 0 WHEN 'REJECTED' THEN 1 ELSE 2 END,wr.end_date DESC,wr.id DESC";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mentorId);
            if (internId == null) { statement.setNull(2, Types.BIGINT); statement.setNull(3, Types.BIGINT); }
            else { statement.setLong(2, internId); statement.setLong(3, internId); }
            statement.setString(4, status); statement.setString(5, status);
            return rows(statement);
        }
    }

    public List<Map<String, Object>> recentNotifications(long userId) throws SQLException {
        String sql = "SELECT id,title,message,target_url targetUrl,is_read `read`,created_at FROM notifications WHERE user_id=? ORDER BY created_at DESC,id DESC LIMIT 5";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            List<Map<String, Object>> notifications = rows(statement);
            for (Map<String, Object> notification : notifications) {
                Object value = notification.get("read");
                boolean read = value instanceof Boolean booleanValue
                        ? booleanValue
                        : value instanceof Number number && number.intValue() != 0;
                notification.put("read", read);
            }
            return notifications;
        }
    }

    public int unreadNotificationCount(long userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0")) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) { result.next(); return result.getInt(1); }
        }
    }

    public void review(long mentorId, long actorUserId, long reportId, String decision,
                       Double score, String feedback, String ipAddress) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long internUserId;
                try (PreparedStatement lock = connection.prepareStatement(
                        "SELECT ip.user_id FROM weekly_reports wr JOIN intern_profiles ip ON ip.id=wr.intern_profile_id WHERE wr.id=? AND ip.mentor_id=? FOR UPDATE")) {
                    lock.setLong(1, reportId); lock.setLong(2, mentorId);
                    try (ResultSet result = lock.executeQuery()) {
                        if (!result.next()) throw new IllegalArgumentException("Báo cáo không thuộc phạm vi phụ trách của bạn.");
                        internUserId = result.getLong(1);
                    }
                }
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE weekly_reports SET status=?,mentor_score=?,mentor_feedback=? WHERE id=?")) {
                    update.setString(1, decision);
                    if (score == null) update.setNull(2, Types.DECIMAL); else update.setDouble(2, score);
                    update.setString(3, feedback); update.setLong(4, reportId); update.executeUpdate();
                }
                String title = "APPROVED".equals(decision) ? "Báo cáo tuần đã được duyệt" : "Mentor yêu cầu cập nhật báo cáo";
                try (PreparedStatement notification = connection.prepareStatement(
                        "INSERT INTO notifications(user_id,title,message,target_url,is_read) VALUES(?,?,?,?,0)")) {
                    notification.setLong(1, internUserId); notification.setString(2, title);
                    notification.setString(3, feedback); notification.setString(4, "/home"); notification.executeUpdate();
                }
                try (PreparedStatement audit = connection.prepareStatement(
                        "INSERT INTO audit_logs(user_id,action_code,table_name,record_id,new_values_json,ip_address) VALUES(?,'REVIEW_WEEKLY_REPORT','weekly_reports',?,JSON_OBJECT('status',?,'score',?,'feedback',?),?)")) {
                    audit.setLong(1, actorUserId); audit.setLong(2, reportId); audit.setString(3, decision);
                    if (score == null) audit.setNull(4, Types.DECIMAL); else audit.setDouble(4, score);
                    audit.setString(5, feedback); audit.setString(6, ipAddress); audit.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback(); throw exception;
            } finally { connection.setAutoCommit(true); }
        }
    }

    private List<Map<String, Object>> rows(PreparedStatement statement) throws SQLException {
        List<Map<String, Object>> values = new ArrayList<>();
        try (ResultSet result = statement.executeQuery()) { while (result.next()) values.add(row(result)); }
        return values;
    }

    private Map<String, Object> row(ResultSet result) throws SQLException {
        Map<String, Object> value = new LinkedHashMap<>();
        for (int index = 1; index <= result.getMetaData().getColumnCount(); index++)
            value.put(result.getMetaData().getColumnLabel(index), result.getObject(index));
        return value;
    }
}
