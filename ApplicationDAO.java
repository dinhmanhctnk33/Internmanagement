package com.example.internmanagement.dao;

import com.example.internmanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationDAO {

    public List<Map<String, Object>> findPrograms() throws SQLException {
        String sql = """
                SELECT p.id, p.program_name, d.dept_name, p.quota_count AS quota,
                       p.start_date, p.end_date,
                       CASE WHEN p.status = 'ACTIVE' THEN 1 ELSE 0 END AS is_open
                FROM internship_programs p
                LEFT JOIN departments d ON d.id = p.department_id
                ORDER BY p.start_date DESC, p.id DESC
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            return mapRows(results);
        }
    }

    public List<Map<String, Object>> findMine(int userId) throws SQLException {
        String sql = """
                SELECT a.id, a.program_id, p.program_name, a.university_name, a.major_name,
                       a.study_year, a.gpa, a.cover_letter, a.status,
                       a.submitted_at, a.updated_at
                FROM internship_applications a
                JOIN internship_programs p ON p.id = a.program_id
                WHERE a.applicant_user_id = ?
                ORDER BY a.updated_at DESC, a.id DESC
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                return mapRows(results);
            }
        }
    }

    public List<Map<String, Object>> findForHr(String status, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT a.id, a.program_id, a.applicant_user_id, u.full_name, u.email,
                       p.program_name, d.dept_name, a.university_name, a.major_name,
                       a.study_year, a.gpa, a.cover_letter, a.status, a.submitted_at,
                       a.updated_at
                FROM internship_applications a
                JOIN users u ON u.id = a.applicant_user_id
                JOIN internship_programs p ON p.id = a.program_id
                LEFT JOIN departments d ON d.id = p.department_id
                WHERE 1 = 1
                """);
        List<Object> parameters = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            if (!List.of("DRAFT", "SUBMITTED", "UNDER_REVIEW", "APPROVED", "REJECTED").contains(status)) {
                throw new IllegalArgumentException("Trạng thái lọc hồ sơ không hợp lệ.");
            }
            sql.append(" AND a.status = ?");
            parameters.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ?)");
            String query = "%" + keyword.trim().toLowerCase() + "%";
            parameters.add(query);
            parameters.add(query);
        }
        sql.append(" ORDER BY a.updated_at DESC, a.id DESC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            try (ResultSet results = statement.executeQuery()) {
                return mapRows(results);
            }
        }
    }

    public void save(Connection connection, int userId, int programId, String university,
                     String major, Integer studyYear, Double gpa, String coverLetter,
                     boolean submit) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            String programSql = "SELECT status FROM internship_programs WHERE id = ? FOR UPDATE";
            try (PreparedStatement statement = connection.prepareStatement(programSql)) {
                statement.setInt(1, programId);
                try (ResultSet results = statement.executeQuery()) {
                    if (!results.next() || !"ACTIVE".equals(results.getString("status"))) {
                        throw new IllegalArgumentException("Chương trình không tồn tại hoặc đã ngừng nhận hồ sơ.");
                    }
                }
            }

            String existingStatus = null;
            long applicationId = 0;
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, status FROM internship_applications WHERE applicant_user_id = ? AND program_id = ? FOR UPDATE")) {
                statement.setInt(1, userId);
                statement.setInt(2, programId);
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        applicationId = results.getLong("id");
                        existingStatus = results.getString("status");
                    }
                }
            }

            if (existingStatus != null && !"DRAFT".equals(existingStatus)) {
                throw new IllegalStateException("Hồ sơ đã được nộp hoặc đã có quyết định; không thể ghi đè.");
            }

            if (existingStatus == null) {
                String insertSql = """
                        INSERT INTO internship_applications
                            (applicant_user_id, program_id, university_name, major_name, study_year,
                             gpa, cover_letter, status, submitted_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    setApplicationParameters(statement, userId, programId, university, major,
                            studyYear, gpa, coverLetter, submit, 1);
                    statement.executeUpdate();
                }
            } else {
                String updateSql = """
                        UPDATE internship_applications
                        SET university_name = ?, major_name = ?, study_year = ?, gpa = ?,
                            cover_letter = ?, status = ?,
                            submitted_at = CASE WHEN ? = 1 THEN CURRENT_TIMESTAMP ELSE NULL END,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ? AND status = 'DRAFT'
                        """;
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setString(1, university);
                    statement.setString(2, major);
                    setNullableInt(statement, 3, studyYear);
                    setNullableDouble(statement, 4, gpa);
                    statement.setString(5, coverLetter);
                    statement.setString(6, submit ? "SUBMITTED" : "DRAFT");
                    statement.setInt(7, submit ? 1 : 0);
                    statement.setLong(8, applicationId);
                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException("Hồ sơ đã thay đổi; hãy tải lại trang trước khi lưu.");
                    }
                }
            }
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public boolean decide(Connection connection, long applicationId, int reviewerId,
                          String decision, String reason) throws SQLException {
        if (!"APPROVED".equals(decision) && !"REJECTED".equals(decision)) {
            throw new IllegalArgumentException("Quyết định không hợp lệ.");
        }
        if ("REJECTED".equals(decision) && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
        }

        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            String previousStatus = null;
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT status FROM internship_applications WHERE id = ? FOR UPDATE")) {
                statement.setLong(1, applicationId);
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        previousStatus = results.getString("status");
                    }
                }
            }
            if (!"SUBMITTED".equals(previousStatus) && !"UNDER_REVIEW".equals(previousStatus)) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE internship_applications SET status = ?, updated_at = CURRENT_TIMESTAMP "
                            + "WHERE id = ? AND status IN ('SUBMITTED','UNDER_REVIEW')")) {
                statement.setString(1, decision);
                statement.setLong(2, applicationId);
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO application_decision_history
                        (application_id, reviewer_user_id, previous_status, decision_status, reason)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {
                statement.setLong(1, applicationId);
                statement.setInt(2, reviewerId);
                statement.setString(3, previousStatus);
                statement.setString(4, decision);
                statement.setString(5, reason == null || reason.isBlank() ? null : reason.trim());
                statement.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static void setApplicationParameters(PreparedStatement statement, int userId, int programId,
                                                   String university, String major, Integer studyYear,
                                                   Double gpa, String coverLetter, boolean submit,
                                                   int offset) throws SQLException {
        statement.setInt(offset, userId);
        statement.setInt(offset + 1, programId);
        statement.setString(offset + 2, university);
        statement.setString(offset + 3, major);
        setNullableInt(statement, offset + 4, studyYear);
        setNullableDouble(statement, offset + 5, gpa);
        statement.setString(offset + 6, coverLetter);
        statement.setString(offset + 7, submit ? "SUBMITTED" : "DRAFT");
        if (submit) {
            statement.setTimestamp(offset + 8, new java.sql.Timestamp(System.currentTimeMillis()));
        } else {
            statement.setNull(offset + 8, Types.TIMESTAMP);
        }
    }

    private static void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) statement.setNull(index, Types.INTEGER);
        else statement.setInt(index, value);
    }

    private static void setNullableDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) statement.setNull(index, Types.DECIMAL);
        else statement.setDouble(index, value);
    }

    private static List<Map<String, Object>> mapRows(ResultSet results) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        int columnCount = results.getMetaData().getColumnCount();
        while (results.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(results.getMetaData().getColumnLabel(i), results.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}