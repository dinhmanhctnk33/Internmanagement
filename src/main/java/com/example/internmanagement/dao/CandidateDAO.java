package com.example.internmanagement.dao;

import com.example.internmanagement.model.Candidate;
import com.example.internmanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CandidateDAO {

    public Candidate findByUserId(int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findByUserId(connection, userId);
        }
    }

    public Candidate findByUserId(Connection connection, int userId) throws SQLException {
        String sql = "SELECT * FROM candidates WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? mapCandidate(results) : null;
            }
        }
    }

    public Candidate findByFileUrl(String fileUrl) throws SQLException {
        String sql = "SELECT * FROM candidates WHERE cv_file_url = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fileUrl);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? mapCandidate(results) : null;
            }
        }
    }

    public void save(Candidate candidate) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            save(connection, candidate);
        }
    }

    public void save(Connection connection, Candidate candidate) throws SQLException {
        Candidate existing = findByUserId(connection, candidate.getUserId());
        if (existing == null) {
            String sql = """
                    INSERT INTO candidates
                    (user_id, full_name, email, phone, university_name, major_name,
                     desired_position, cv_file_url, application_status, hr_notes, applied_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'NEW', NULL, ?)
                    """;
            try (PreparedStatement statement =
                         connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                bindApplication(statement, candidate, false);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Không nhận được mã hồ sơ ứng tuyển mới.");
                    }
                    candidate.setId(keys.getInt(1));
                }
            }
            candidate.setApplicationStatus("NEW");
            candidate.setHrNotes(null);
            candidate.setAppliedAt(LocalDateTime.now());
            return;
        }

        String sql = """
                UPDATE candidates
                SET full_name = ?, email = ?, phone = ?, university_name = ?, major_name = ?,
                    desired_position = ?, cv_file_url = ?, application_status = 'NEW',
                    hr_notes = NULL, applied_at = ?
                WHERE user_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindApplication(statement, candidate, true);
            statement.setInt(9, candidate.getUserId());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Không thể cập nhật hồ sơ ứng tuyển.");
            }
        }
        candidate.setId(existing.getId());
        candidate.setApplicationStatus("NEW");
        candidate.setHrNotes(null);
        candidate.setAppliedAt(LocalDateTime.now());
    }

    private void bindApplication(PreparedStatement statement, Candidate candidate, boolean update)
            throws SQLException {
        int offset = update ? 0 : 1;
        if (!update) {
            statement.setInt(1, candidate.getUserId());
        }
        statement.setString(1 + offset, candidate.getFullName());
        statement.setString(2 + offset, candidate.getEmail());
        statement.setString(3 + offset, candidate.getPhone());
        statement.setString(4 + offset, candidate.getUniversityName());
        statement.setString(5 + offset, candidate.getMajorName());
        statement.setString(6 + offset, candidate.getDesiredPosition());
        statement.setString(7 + offset, candidate.getCvFileUrl());
        statement.setTimestamp(8 + offset, Timestamp.valueOf(LocalDateTime.now()));
    }

    private Candidate mapCandidate(ResultSet results) throws SQLException {
        Candidate candidate = new Candidate();
        candidate.setId(results.getInt("id"));
        candidate.setUserId(results.getInt("user_id"));
        candidate.setFullName(results.getString("full_name"));
        candidate.setEmail(results.getString("email"));
        candidate.setPhone(results.getString("phone"));
        candidate.setUniversityName(results.getString("university_name"));
        candidate.setMajorName(results.getString("major_name"));
        candidate.setDesiredPosition(results.getString("desired_position"));
        candidate.setCvFileUrl(results.getString("cv_file_url"));
        candidate.setApplicationStatus(results.getString("application_status"));
        candidate.setHrNotes(results.getString("hr_notes"));
        Timestamp appliedAt = results.getTimestamp("applied_at");
        if (appliedAt != null) {
            candidate.setAppliedAt(appliedAt.toLocalDateTime());
        }
        return candidate;
    }
}
