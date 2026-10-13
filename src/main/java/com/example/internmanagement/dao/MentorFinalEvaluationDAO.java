package com.example.internmanagement.dao;

import com.example.internmanagement.util.DBConnection;
import com.example.internmanagement.util.FinalEvaluationRules;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MentorFinalEvaluationDAO {
    public Long findActiveMentorId(long userId) throws SQLException {
        String sql = "SELECT id FROM mentors WHERE user_id=? AND status='ACTIVE'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong("id") : null;
            }
        }
    }

    public List<Map<String, Object>> assignedInterns(long mentorId) throws SQLException {
        String sql = """
                SELECT ip.id, ip.intern_code AS internCode, u.full_name AS fullName,
                       ip.major_name AS majorName, ip.start_date AS startDate, ip.end_date AS endDate,
                       CASE WHEN ip.start_date IS NULL OR ip.end_date IS NULL OR ip.end_date < ip.start_date
                            THEN NULL
                            ELSE DATE_ADD(ip.start_date, INTERVAL (FLOOR(DATEDIFF(ip.end_date, ip.start_date) / 2) + 1) DAY)
                       END AS eligibleAt,
                       e.status AS evaluationStatus
                FROM intern_profiles ip
                JOIN users u ON u.id=ip.user_id AND u.status='ACTIVE'
                LEFT JOIN mentor_final_evaluations e ON e.intern_profile_id=ip.id
                WHERE ip.mentor_id=? AND ip.internship_status IN ('ACTIVE','COMPLETED')
                ORDER BY u.full_name, ip.id
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mentorId);
            try (ResultSet result = statement.executeQuery()) {
                List<Map<String, Object>> interns = new ArrayList<>();
                LocalDate today = LocalDate.now();
                while (result.next()) {
                    Map<String, Object> intern = new LinkedHashMap<>();
                    intern.put("id", result.getLong("id"));
                    intern.put("internCode", result.getString("internCode"));
                    intern.put("fullName", result.getString("fullName"));
                    intern.put("majorName", result.getString("majorName"));
                    intern.put("startDate", result.getDate("startDate"));
                    intern.put("endDate", result.getDate("endDate"));
                    Date eligibleAt = result.getDate("eligibleAt");
                    intern.put("eligibleAt", eligibleAt);
                    intern.put("eligible", eligibleAt != null && !today.isBefore(eligibleAt.toLocalDate()));
                    intern.put("evaluationStatus", result.getString("evaluationStatus"));
                    interns.add(intern);
                }
                return interns;
            }
        }
    }

    public Map<String, Object> findEvaluation(long internProfileId) throws SQLException {
        String sql = """
                SELECT id, intern_profile_id AS internProfileId, status,
                       knowledge_score AS knowledgeScore, problem_solving_score AS problemSolvingScore,
                       initiative_score AS initiativeScore, teamwork_score AS teamworkScore,
                       skill_score AS skillScore, attitude_score AS attitudeScore,
                       overall_score AS overallScore, overall_comments AS overallComments,
                       revision_no AS revisionNo, finalization_count AS finalizationCount,
                       finalized_at AS finalizedAt, updated_at AS updatedAt
                FROM mentor_final_evaluations
                WHERE intern_profile_id=?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, internProfileId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? row(result) : new LinkedHashMap<>();
            }
        }
    }

    public String save(long mentorId, long actorUserId, long internProfileId, int expectedRevision,
                       Integer knowledge, Integer problemSolving, Integer initiative, Integer teamwork,
                       String comment, boolean finalize, String ipAddress) throws SQLException {
        FinalEvaluationRules.validateScore(knowledge);
        FinalEvaluationRules.validateScore(problemSolving);
        FinalEvaluationRules.validateScore(initiative);
        FinalEvaluationRules.validateScore(teamwork);
        String normalizedComment = comment == null ? "" : comment.trim();
        if (normalizedComment.length() > FinalEvaluationRules.MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("Nhận xét chung không được vượt quá 2000 ký tự.");
        }
        if (finalize) {
            FinalEvaluationRules.validateForFinalization(
                    knowledge, problemSolving, initiative, teamwork, normalizedComment);
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                lockEligibleIntern(connection, mentorId, internProfileId);
                Map<String, Object> previous = lockEvaluation(connection, internProfileId);
                int currentRevision = previous.isEmpty() ? 0 : (Integer) previous.get("revisionNo");
                if (expectedRevision != currentRevision) {
                    throw new IllegalArgumentException(
                            "Đánh giá đã được thay đổi ở nơi khác. Hãy tải lại trang trước khi lưu.");
                }

                int previousFinalizations = previous.isEmpty()
                        ? 0 : (Integer) previous.get("finalizationCount");
                String status = finalize
                        ? (previousFinalizations == 0 ? "FINALIZED" : "FINALIZED_AGAIN")
                        : "DRAFT";
                int finalizationCount = previousFinalizations + (finalize ? 1 : 0);
                int nextRevision = currentRevision + 1;
                if (!previous.isEmpty()) {
                    archivePrevious(connection, previous, actorUserId, finalize ? "FINALIZE" : "SAVE_DRAFT");
                    updateEvaluation(connection, mentorId, internProfileId, status, knowledge,
                            problemSolving, initiative, teamwork, normalizedComment,
                            finalizationCount, nextRevision, finalize);
                } else {
                    insertEvaluation(connection, mentorId, internProfileId, status, knowledge,
                            problemSolving, initiative, teamwork, normalizedComment,
                            finalizationCount, nextRevision, finalize);
                }
                insertAudit(connection, actorUserId, internProfileId, previous, status,
                        knowledge, problemSolving, initiative, teamwork, normalizedComment, ipAddress);
                connection.commit();
                return status;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void lockEligibleIntern(Connection connection, long mentorId, long internProfileId)
            throws SQLException {
        String sql = """
                SELECT internship_status, start_date, end_date
                FROM intern_profiles
                WHERE id=? AND mentor_id=?
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, internProfileId);
            statement.setLong(2, mentorId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                            "Thực tập sinh không thuộc phạm vi đang hướng dẫn của bạn.");
                }
                String status = result.getString("internship_status");
                if (!"ACTIVE".equals(status) && !"COMPLETED".equals(status)) {
                    throw new IllegalArgumentException(
                            "Chỉ có thể đánh giá thực tập sinh đang thực tập hoặc đã hoàn thành.");
                }
                Date start = result.getDate("start_date");
                Date end = result.getDate("end_date");
                LocalDate eligibleAt = FinalEvaluationRules.eligibleAt(
                        start == null ? null : start.toLocalDate(),
                        end == null ? null : end.toLocalDate());
                if (eligibleAt == null || LocalDate.now().isBefore(eligibleAt)) {
                    throw new IllegalArgumentException(
                            "Chỉ có thể đánh giá khi thực tập sinh đã đến giữa kỳ thực tập.");
                }
            }
        }
    }

    private Map<String, Object> lockEvaluation(Connection connection, long internProfileId)
            throws SQLException {
        String sql = """
                SELECT id, status, knowledge_score AS knowledgeScore,
                       problem_solving_score AS problemSolvingScore,
                       initiative_score AS initiativeScore, teamwork_score AS teamworkScore,
                       skill_score AS skillScore, attitude_score AS attitudeScore,
                       overall_score AS overallScore, overall_comments AS overallComments,
                       revision_no AS revisionNo, finalization_count AS finalizationCount
                FROM mentor_final_evaluations
                WHERE intern_profile_id=?
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, internProfileId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? row(result) : new LinkedHashMap<>();
            }
        }
    }

    private void archivePrevious(Connection connection, Map<String, Object> previous,
                                 long actorUserId, String action) throws SQLException {
        String sql = """
                INSERT INTO mentor_final_evaluation_history
                    (evaluation_id, revision_no, actor_user_id, action_code, status,
                     knowledge_score, problem_solving_score, initiative_score, teamwork_score,
                     skill_score, attitude_score, overall_score, overall_comments)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ((Number) previous.get("id")).longValue());
            statement.setInt(2, (Integer) previous.get("revisionNo"));
            statement.setLong(3, actorUserId);
            statement.setString(4, action);
            statement.setString(5, (String) previous.get("status"));
            setInteger(statement, 6, (Integer) previous.get("knowledgeScore"));
            setInteger(statement, 7, (Integer) previous.get("problemSolvingScore"));
            setInteger(statement, 8, (Integer) previous.get("initiativeScore"));
            setInteger(statement, 9, (Integer) previous.get("teamworkScore"));
            setDecimal(statement, 10, previous.get("skillScore"));
            setDecimal(statement, 11, previous.get("attitudeScore"));
            setDecimal(statement, 12, previous.get("overallScore"));
            statement.setString(13, (String) previous.get("overallComments"));
            statement.executeUpdate();
        }
    }

    private void insertEvaluation(Connection connection, long mentorId, long internProfileId,
                                  String status, Integer knowledge, Integer problemSolving,
                                  Integer initiative, Integer teamwork, String comment,
                                  int finalizationCount, int revision, boolean finalize)
            throws SQLException {
        String sql = """
                INSERT INTO mentor_final_evaluations
                    (intern_profile_id, mentor_id, status, knowledge_score, problem_solving_score,
                     initiative_score, teamwork_score, skill_score, attitude_score, overall_score,
                     overall_comments, revision_no, finalization_count, finalized_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindEvaluation(statement, mentorId, internProfileId, status, knowledge, problemSolving,
                    initiative, teamwork, comment, finalizationCount, revision, finalize);
            statement.executeUpdate();
        }
    }

    private void updateEvaluation(Connection connection, long mentorId, long internProfileId,
                                  String status, Integer knowledge, Integer problemSolving,
                                  Integer initiative, Integer teamwork, String comment,
                                  int finalizationCount, int revision, boolean finalize)
            throws SQLException {
        String sql = """
                UPDATE mentor_final_evaluations
                SET mentor_id=?, status=?, knowledge_score=?, problem_solving_score=?,
                    initiative_score=?, teamwork_score=?, skill_score=?, attitude_score=?,
                    overall_score=?, overall_comments=?, revision_no=?, finalization_count=?,
                    finalized_at=IF(?, CURRENT_TIMESTAMP, NULL)
                WHERE intern_profile_id=?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mentorId);
            statement.setString(2, status);
            setInteger(statement, 3, knowledge);
            setInteger(statement, 4, problemSolving);
            setInteger(statement, 5, initiative);
            setInteger(statement, 6, teamwork);
            setDecimal(statement, 7, FinalEvaluationRules.average(knowledge, problemSolving));
            setDecimal(statement, 8, FinalEvaluationRules.average(initiative, teamwork));
            setDecimal(statement, 9,
                    FinalEvaluationRules.average(knowledge, problemSolving, initiative, teamwork));
            statement.setString(10, comment);
            statement.setInt(11, revision);
            statement.setInt(12, finalizationCount);
            statement.setBoolean(13, finalize);
            statement.setLong(14, internProfileId);
            statement.executeUpdate();
        }
    }

    private void bindEvaluation(PreparedStatement statement, long mentorId, long internProfileId,
                               String status, Integer knowledge, Integer problemSolving,
                               Integer initiative, Integer teamwork, String comment,
                               int finalizationCount, int revision, boolean finalize)
            throws SQLException {
        statement.setLong(1, internProfileId);
        statement.setLong(2, mentorId);
        statement.setString(3, status);
        setInteger(statement, 4, knowledge);
        setInteger(statement, 5, problemSolving);
        setInteger(statement, 6, initiative);
        setInteger(statement, 7, teamwork);
        setDecimal(statement, 8, FinalEvaluationRules.average(knowledge, problemSolving));
        setDecimal(statement, 9, FinalEvaluationRules.average(initiative, teamwork));
        setDecimal(statement, 10,
                FinalEvaluationRules.average(knowledge, problemSolving, initiative, teamwork));
        statement.setString(11, comment);
        statement.setInt(12, revision);
        statement.setInt(13, finalizationCount);
        if (finalize) {
            statement.setTimestamp(14, new java.sql.Timestamp(System.currentTimeMillis()));
        } else {
            statement.setNull(14, Types.TIMESTAMP);
        }
    }

    private void insertAudit(Connection connection, long actorUserId, long internProfileId,
                             Map<String, Object> previous, String status, Integer knowledge,
                             Integer problemSolving, Integer initiative, Integer teamwork,
                             String comment, String ipAddress) throws SQLException {
        String sql = """
                INSERT INTO audit_logs
                    (user_id, action_code, table_name, record_id, old_values_json, new_values_json, ip_address)
                VALUES (?, 'SAVE_MENTOR_FINAL_EVALUATION', 'mentor_final_evaluations', ?,
                        IF(? IS NULL, NULL, JSON_OBJECT('status', ?, 'knowledgeScore', ?,
                           'problemSolvingScore', ?, 'initiativeScore', ?, 'teamworkScore', ?, 'comments', ?)),
                        JSON_OBJECT('status', ?, 'knowledgeScore', ?, 'problemSolvingScore', ?,
                           'initiativeScore', ?, 'teamworkScore', ?, 'comments', ?), ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, actorUserId);
            statement.setLong(2, internProfileId);
            Object previousId = previous.get("id");
            if (previousId == null) {
                statement.setNull(3, Types.BIGINT);
                statement.setNull(4, Types.VARCHAR);
                statement.setNull(5, Types.INTEGER);
                statement.setNull(6, Types.INTEGER);
                statement.setNull(7, Types.INTEGER);
                statement.setNull(8, Types.INTEGER);
                statement.setNull(9, Types.VARCHAR);
            } else {
                statement.setLong(3, ((Number) previousId).longValue());
                statement.setString(4, (String) previous.get("status"));
                setNullableInt(statement, 5, previous.get("knowledgeScore"));
                setNullableInt(statement, 6, previous.get("problemSolvingScore"));
                setNullableInt(statement, 7, previous.get("initiativeScore"));
                setNullableInt(statement, 8, previous.get("teamworkScore"));
                statement.setString(9, (String) previous.get("overallComments"));
            }
            statement.setString(10, status);
            setInteger(statement, 11, knowledge);
            setInteger(statement, 12, problemSolving);
            setInteger(statement, 13, initiative);
            setInteger(statement, 14, teamwork);
            statement.setString(15, comment);
            statement.setString(16, ipAddress);
            statement.executeUpdate();
        }
    }

    private void setInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) statement.setNull(index, Types.TINYINT);
        else statement.setInt(index, value);
    }

    private void setNullableInt(PreparedStatement statement, int index, Object value) throws SQLException {
        if (value == null) statement.setNull(index, Types.INTEGER);
        else statement.setInt(index, ((Number) value).intValue());
    }

    private void setDecimal(PreparedStatement statement, int index, Object value) throws SQLException {
        if (value == null) statement.setNull(index, Types.DECIMAL);
        else if (value instanceof java.math.BigDecimal decimal) statement.setBigDecimal(index, decimal);
        else statement.setBigDecimal(index, new java.math.BigDecimal(value.toString()));
    }

    private Map<String, Object> row(ResultSet result) throws SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int column = 1; column <= result.getMetaData().getColumnCount(); column++) {
            values.put(result.getMetaData().getColumnLabel(column), result.getObject(column));
        }
        return values;
    }
}
