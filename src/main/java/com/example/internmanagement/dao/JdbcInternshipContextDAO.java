package com.example.internmanagement.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class JdbcInternshipContextDAO implements IInternshipContextDAO {
    @Override
    public List<MeetingDto> getActiveMeetingsBetween(Connection connection, LocalDateTime start, LocalDateTime end)
            throws SQLException {
        String sql = "SELECT id, title FROM meetings WHERE status = 'SCHEDULED' "
                + "AND starts_at >= ? AND starts_at < ? ORDER BY starts_at, id";
        List<MeetingDto> meetings = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(start));
            statement.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    meetings.add(new MeetingDto(results.getLong("id"), results.getString("title")));
                }
            }
        }
        return meetings;
    }

    @Override
    public List<Integer> getInternAttendeesForMeeting(Connection connection, long meetingId) throws SQLException {
        String sql = "SELECT ip.user_id FROM meeting_attendees ma "
            + "JOIN meetings m ON m.id = ma.meeting_id "
                + "JOIN intern_profiles ip ON ip.id = ma.intern_profile_id "
                + "WHERE ma.meeting_id = ? AND ip.internship_status = 'ACTIVE' "
            + "AND (ip.start_date IS NULL OR ip.start_date <= DATE(m.starts_at)) "
            + "AND (ip.end_date IS NULL OR ip.end_date >= DATE(m.starts_at))";
        List<Integer> attendees = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, meetingId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    attendees.add(results.getInt("user_id"));
                }
            }
        }
        return attendees;
    }

    @Override
    public List<TaskDto> getIncompleteTasksDueOn(Connection connection, LocalDate date) throws SQLException {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        String sql = "SELECT t.id, t.task_title, ip.user_id FROM tasks t "
                + "JOIN intern_profiles ip ON ip.id = t.intern_profile_id "
                + "WHERE t.due_date >= ? AND t.due_date < ? "
                + "AND t.status NOT IN ('COMPLETED', 'VERIFIED') "
                + "AND ip.internship_status = 'ACTIVE' "
                + "AND (ip.start_date IS NULL OR ip.start_date <= ?) "
                + "AND (ip.end_date IS NULL OR ip.end_date >= ?)";
        List<TaskDto> tasks = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(start));
            statement.setTimestamp(2, Timestamp.valueOf(end));
            statement.setDate(3, Date.valueOf(date.minusDays(1)));
            statement.setDate(4, Date.valueOf(date.minusDays(1)));
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    tasks.add(new TaskDto(results.getLong("id"), results.getInt("user_id"),
                            results.getString("task_title")));
                }
            }
        }
        return tasks;
    }

    @Override
    public List<WeeklyReportReminderDto> getInternsMissingWeeklyReport(Connection connection, LocalDate today)
            throws SQLException {
        String sql = "SELECT ip.id, ip.user_id, ip.start_date FROM intern_profiles ip "
                + "WHERE ip.internship_status = 'ACTIVE' "
                + "AND (ip.start_date IS NULL OR ip.start_date <= ?) "
                + "AND (ip.end_date IS NULL OR ip.end_date >= ?)";
        List<WeeklyReportReminderDto> recipients = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(today));
            statement.setDate(2, Date.valueOf(today));
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    int internProfileId = results.getInt("id");
                    Date startDate = results.getDate("start_date");
                    int weekNumber = startDate == null ? 1
                            : (int) (ChronoUnit.DAYS.between(startDate.toLocalDate(), today) / 7) + 1;
                    if (!hasSubmittedReport(connection, internProfileId, weekNumber)) {
                        recipients.add(new WeeklyReportReminderDto(results.getInt("user_id"), weekNumber));
                    }
                }
            }
        }
        return recipients;
    }

    @Override
    public boolean isMeetingActiveAndAccessible(Connection connection, long meetingId, int internId)
            throws SQLException {
        String sql = "SELECT 1 FROM meetings m "
                + "JOIN meeting_attendees ma ON ma.meeting_id = m.id "
                + "JOIN intern_profiles ip ON ip.id = ma.intern_profile_id "
                + "WHERE m.id = ? AND m.status = 'SCHEDULED' AND ip.user_id = ? "
                + "AND ip.internship_status = 'ACTIVE' "
                + "AND (ip.start_date IS NULL OR ip.start_date <= DATE(m.starts_at)) "
                + "AND (ip.end_date IS NULL OR ip.end_date >= DATE(m.starts_at))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, meetingId);
            statement.setInt(2, internId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    @Override
    public boolean isTaskActiveAndAssigned(Connection connection, long taskId, int internId) throws SQLException {
        String sql = "SELECT 1 FROM tasks t JOIN intern_profiles ip ON ip.id = t.intern_profile_id "
                + "WHERE t.id = ? AND ip.user_id = ? AND ip.internship_status = 'ACTIVE' "
                + "AND t.status NOT IN ('COMPLETED', 'VERIFIED')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            statement.setInt(2, internId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    private boolean hasSubmittedReport(Connection connection, int internProfileId, int weekNumber)
            throws SQLException {
        String sql = "SELECT 1 FROM weekly_reports WHERE intern_profile_id = ? AND week_number = ? "
                + "AND status IN ('SUBMITTED', 'APPROVED')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, internProfileId);
            statement.setInt(2, weekNumber);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }
}