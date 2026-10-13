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

    public List<Map<String, Object>> evaluations(long mentorId) throws SQLException {
        String sql = "SELECT ip.id intern_id,ip.intern_code,u.full_name,u.email,p.program_name," +
                "e.technical_score skill_score,e.attitude_score,e.punctuality_score,e.final_score,e.final_grade," +
                "e.recommend_hiring,e.overall_comments,e.hr_approval_status " +
                "FROM intern_profiles ip JOIN users u ON u.id=ip.user_id " +
                "LEFT JOIN internship_programs p ON p.id=ip.program_id " +
                "LEFT JOIN intern_evaluations e ON e.intern_profile_id=ip.id " +
                "WHERE ip.mentor_id=? AND ip.internship_status='ACTIVE' ORDER BY u.full_name";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mentorId);
            return rows(statement);
        }
    }

    public Map<String, Object> evaluation(long mentorId, Long internId) throws SQLException {
        if (internId == null) return null;
        String sql = "SELECT ip.id intern_id,ip.intern_code,u.full_name,u.email,p.program_name," +
                "e.technical_score skill_score,e.attitude_score,e.punctuality_score,e.final_score,e.final_grade," +
                "e.recommend_hiring,e.overall_comments,e.hr_approval_status " +
                "FROM intern_profiles ip JOIN users u ON u.id=ip.user_id " +
                "LEFT JOIN internship_programs p ON p.id=ip.program_id " +
                "LEFT JOIN intern_evaluations e ON e.intern_profile_id=ip.id " +
                "WHERE ip.mentor_id=? AND ip.id=? AND ip.internship_status='ACTIVE'";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mentorId);
            statement.setLong(2, internId);
            List<Map<String, Object>> result = rows(statement);
            return result.isEmpty() ? null : result.get(0);
        }
    }

    public void saveEvaluation(long mentorId, long internId, double skillScore, double attitudeScore,
                               double punctualityScore, String comments, boolean recommendHiring,
                               long actorUserId) throws SQLException {
        double finalScore = Math.round((skillScore * 0.5 + attitudeScore * 0.3 + punctualityScore * 0.2) * 100.0) / 100.0;
        String grade = finalScore >= 9 ? "EXCELLENT" : finalScore >= 8 ? "GOOD" : finalScore >= 5 ? "PASS" : "FAIL";
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long internUserId;
                try (PreparedStatement lock = connection.prepareStatement(
                        "SELECT user_id FROM intern_profiles WHERE id=? AND mentor_id=? AND internship_status='ACTIVE' FOR UPDATE")) {
                    lock.setLong(1, internId);
                    lock.setLong(2, mentorId);
                    try (ResultSet result = lock.executeQuery()) {
                        if (!result.next()) throw new IllegalArgumentException("Thực tập sinh không thuộc phạm vi phụ trách của bạn.");
                        internUserId = result.getLong(1);
                    }
                }
                Long evaluationId = null;
                try (PreparedStatement find = connection.prepareStatement(
                        "SELECT id FROM intern_evaluations WHERE intern_profile_id=? FOR UPDATE")) {
                    find.setLong(1, internId);
                    try (ResultSet result = find.executeQuery()) { if (result.next()) evaluationId = result.getLong(1); }
                }
                if (evaluationId == null) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO intern_evaluations(intern_profile_id,mentor_id,technical_score,attitude_score,punctuality_score,final_score,final_grade,recommend_hiring,overall_comments,hr_approval_status) VALUES(?,?,?,?,?,?,?,?,?,'PENDING')")) {
                        insert.setLong(1, internId);
                        insert.setLong(2, mentorId);
                        insert.setDouble(3, skillScore);
                        insert.setDouble(4, attitudeScore);
                        insert.setDouble(5, punctualityScore);
                        insert.setDouble(6, finalScore);
                        insert.setString(7, grade);
                        insert.setBoolean(8, recommendHiring);
                        insert.setString(9, comments);
                        insert.executeUpdate();
                    }
                } else {
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE intern_evaluations SET mentor_id=?,technical_score=?,attitude_score=?,punctuality_score=?,final_score=?,final_grade=?,recommend_hiring=?,overall_comments=?,hr_approval_status='PENDING' WHERE id=?")) {
                        update.setLong(1, mentorId);
                        update.setDouble(2, skillScore);
                        update.setDouble(3, attitudeScore);
                        update.setDouble(4, punctualityScore);
                        update.setDouble(5, finalScore);
                        update.setString(6, grade);
                        update.setBoolean(7, recommendHiring);
                        update.setString(8, comments);
                        update.setLong(9, evaluationId);
                        update.executeUpdate();
                    }
                }
                try (PreparedStatement notification = connection.prepareStatement(
                        "INSERT INTO notifications(user_id,title,message,target_url,is_read) VALUES(?,?,?,?,0)")) {
                    notification.setLong(1, internUserId);
                    notification.setString(2, "Mentor đã hoàn tất đánh giá tổng kết");
                    notification.setString(3, "Điểm tổng kết của bạn: " + finalScore + "/10. Kết quả đang chờ HR xác nhận.");
                    notification.setString(4, "/home");
                    notification.executeUpdate();
                }
                try (PreparedStatement audit = connection.prepareStatement(
                        "INSERT INTO audit_logs(user_id,action_code,table_name,record_id,new_values_json) VALUES(?,'SAVE_INTERN_EVALUATION','intern_evaluations',?,?)")) {
                    audit.setLong(1, actorUserId);
                    audit.setLong(2, internId);
                    audit.setString(3, "{\"final_score\":" + finalScore + ",\"final_grade\":\"" + grade + "\"}");
                    audit.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
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
        String sql = "SELECT id,title,message,target_url targetUrl,is_read=1 `read`,created_at FROM notifications WHERE user_id=? ORDER BY created_at DESC,id DESC LIMIT 5";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId); return rows(statement);
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
            value.put(result.getMetaData().getColumnLabel(index).toLowerCase(Locale.ROOT), result.getObject(index));
        return value;
    }
}
