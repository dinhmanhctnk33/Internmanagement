package com.example.internmanagement.dao;

import com.example.internmanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskDAO {

    public void createTasksForInterns(String title, String description, LocalDate dueDate,
                                      int mentorUserId, List<Integer> internProfileIds) throws SQLException {
        if (internProfileIds == null || internProfileIds.isEmpty()) {
            throw new IllegalArgumentException("At least one intern must be selected");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int mentorId = findActiveMentorId(connection, mentorUserId);
                Set<Integer> assignedIds = findAssignedInternProfileIds(connection, mentorUserId, internProfileIds);
                if (assignedIds.size() != new HashSet<>(internProfileIds).size()) {
                    throw new IllegalArgumentException("Selected intern is not assigned to this mentor");
                }

                String sql = "INSERT INTO tasks (task_title, description, mentor_id, intern_profile_id, due_date, priority, completion_percent, status) "
                        + "VALUES (?, ?, ?, ?, ?, 'MEDIUM', 0, 'TODO')";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (Integer internProfileId : internProfileIds) {
                        statement.setString(1, title.trim());
                        if (description == null || description.isBlank()) {
                            statement.setNull(2, Types.VARCHAR);
                        } else {
                            statement.setString(2, description.trim());
                        }
                        statement.setInt(3, mentorId);
                        statement.setInt(4, internProfileId);
                        if (dueDate == null) {
                            statement.setNull(5, Types.TIMESTAMP);
                        } else {
                            statement.setDate(5, Date.valueOf(dueDate));
                        }
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private int findActiveMentorId(Connection connection, int mentorUserId) throws SQLException {
        String sql = "SELECT id FROM mentors WHERE user_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, mentorUserId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new IllegalArgumentException("Mentor account is not active");
                }
                return results.getInt("id");
            }
        }
    }

    private Set<Integer> findAssignedInternProfileIds(Connection connection, int mentorUserId,
                                                       List<Integer> internProfileIds) throws SQLException {
        String placeholders = String.join(",", java.util.Collections.nCopies(internProfileIds.size(), "?"));
        String sql = "SELECT ip.id FROM intern_profiles ip JOIN mentors m ON m.id = ip.mentor_id "
                + "WHERE m.user_id = ? AND m.status = 'ACTIVE' AND ip.internship_status = 'ACTIVE' "
                + "AND ip.id IN (" + placeholders + ")";
        Set<Integer> assignedIds = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, mentorUserId);
            for (int index = 0; index < internProfileIds.size(); index++) {
                statement.setInt(index + 2, internProfileIds.get(index));
            }
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    assignedIds.add(results.getInt("id"));
                }
            }
        }
        return assignedIds;
    }
}