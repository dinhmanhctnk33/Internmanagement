package com.example.internmanagement.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IInternshipContextDAO {
    List<MeetingDto> getActiveMeetingsBetween(Connection connection, LocalDateTime start, LocalDateTime end)
            throws SQLException;

    List<Integer> getInternAttendeesForMeeting(Connection connection, long meetingId) throws SQLException;

    List<TaskDto> getIncompleteTasksDueOn(Connection connection, LocalDate date) throws SQLException;

    List<WeeklyReportReminderDto> getInternsMissingWeeklyReport(Connection connection, LocalDate today)
            throws SQLException;

    boolean isMeetingActiveAndAccessible(Connection connection, long meetingId, int internId)
            throws SQLException;

    boolean isTaskActiveAndAssigned(Connection connection, long taskId, int internId) throws SQLException;

    record MeetingDto(long id, String title) {
    }

    record TaskDto(long id, int assigneeId, String title) {
    }

    record WeeklyReportReminderDto(int recipientId, int weekNumber) {
    }
}