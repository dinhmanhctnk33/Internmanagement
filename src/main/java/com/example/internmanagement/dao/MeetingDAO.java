package com.example.internmanagement.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MeetingDAO {
    public record MeetingDetails(long id, String title, String description, LocalDateTime startsAt) {
    }

    public long createForMentor(Connection connection, int mentorUserId, String title, String description,
                                LocalDateTime startsAt, List<Integer> internProfileIds) throws SQLException {
        if (mentorUserId <= 0 || title == null || title.isBlank() || title.trim().length() > 200
                || startsAt == null || !startsAt.isAfter(LocalDateTime.now())
                || internProfileIds == null || internProfileIds.isEmpty()) {
            throw new IllegalArgumentException("Thông tin cuộc họp không hợp lệ.");
        }
        Set<Integer> selectedIds = new HashSet<>(internProfileIds);
        if (selectedIds.size() != internProfileIds.size() || selectedIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Danh sách thực tập sinh không hợp lệ.");
        }

        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            ensureActiveMentor(connection, mentorUserId);
            if (findAssignedInterns(connection, mentorUserId, startsAt.toLocalDate(), selectedIds) != selectedIds.size()) {
                throw new IllegalArgumentException("Chỉ có thể mời thực tập sinh đang thuộc nhóm của bạn.");
            }

            long meetingId;
            String insertMeeting = "INSERT INTO meetings "
                    + "(title, description, starts_at, status, created_by_user_id) VALUES (?, ?, ?, 'SCHEDULED', ?)";
            try (PreparedStatement statement = connection.prepareStatement(
                    insertMeeting, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, title.trim());
                if (description == null || description.isBlank()) {
                    statement.setNull(2, Types.VARCHAR);
                } else {
                    statement.setString(2, description.trim());
                }
                statement.setTimestamp(3, Timestamp.valueOf(startsAt));
                statement.setInt(4, mentorUserId);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Không nhận được mã cuộc họp mới.");
                    }
                    meetingId = keys.getLong(1);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO meeting_attendees (meeting_id, intern_profile_id) VALUES (?, ?)")) {
                for (int internProfileId : selectedIds) {
                    statement.setLong(1, meetingId);
                    statement.setInt(2, internProfileId);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            connection.commit();
            return meetingId;
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public MeetingDetails findForAttendee(Connection connection, long meetingId, int userId) throws SQLException {
        String sql = "SELECT m.id, m.title, m.description, m.starts_at FROM meetings m "
                + "JOIN meeting_attendees ma ON ma.meeting_id = m.id "
                + "JOIN intern_profiles ip ON ip.id = ma.intern_profile_id "
                + "WHERE m.id = ? AND m.status = 'SCHEDULED' AND ip.user_id = ? "
                + "AND ip.internship_status = 'ACTIVE' "
                + "AND (ip.start_date IS NULL OR ip.start_date <= DATE(m.starts_at)) "
                + "AND (ip.end_date IS NULL OR ip.end_date >= DATE(m.starts_at))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, meetingId);
            statement.setInt(2, userId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                return new MeetingDetails(results.getLong("id"), results.getString("title"),
                        results.getString("description"), results.getTimestamp("starts_at").toLocalDateTime());
            }
        }
    }

    private void ensureActiveMentor(Connection connection, int mentorUserId) throws SQLException {
        String sql = "SELECT id FROM mentors WHERE user_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, mentorUserId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new IllegalArgumentException("Tài khoản mentor không hoạt động.");
                }
            }
        }
    }

    private int findAssignedInterns(Connection connection, int mentorUserId, java.time.LocalDate startsOn,
                                    Set<Integer> internProfileIds) throws SQLException {
        String placeholders = String.join(",", java.util.Collections.nCopies(internProfileIds.size(), "?"));
        String sql = "SELECT COUNT(*) FROM intern_profiles ip JOIN mentors m ON m.id = ip.mentor_id "
                + "WHERE m.user_id = ? AND m.status = 'ACTIVE' AND ip.internship_status = 'ACTIVE' "
                + "AND (ip.start_date IS NULL OR ip.start_date <= ?) "
                + "AND (ip.end_date IS NULL OR ip.end_date >= ?) AND ip.id IN (" + placeholders + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, mentorUserId);
            statement.setDate(2, Date.valueOf(startsOn));
            statement.setDate(3, Date.valueOf(startsOn));
            int index = 4;
            for (int internProfileId : internProfileIds) {
                statement.setInt(index++, internProfileId);
            }
            try (ResultSet results = statement.executeQuery()) {
                results.next();
                return results.getInt(1);
            }
        }
    }
}