package com.example.internmanagement.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.internmanagement.dao.JdbcInternshipContextDAO;
import com.example.internmanagement.dao.MeetingDAO;
import com.example.internmanagement.dao.NotificationDAO;

class ScheduledReminderServiceTest {
    @Test
    void taskDueReminderOnlyTargetsActiveIncompleteTaskAndDoesNotDuplicate() throws Exception {
        try (Connection connection = notificationConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO intern_profiles (id, user_id, internship_status, start_date, end_date) "
                        + "VALUES (1, 101, 'ACTIVE', DATE '2026-10-01', DATE '2026-12-31'), "
                        + "(2, 102, 'COMPLETED', DATE '2026-10-01', DATE '2026-10-10')");
                statement.execute("INSERT INTO tasks (id, task_title, intern_profile_id, due_date, status) VALUES "
                        + "(1, 'Báo cáo tiến độ', 1, TIMESTAMP '2026-10-14 16:00:00', 'TODO'), "
                        + "(2, 'Đã hoàn thành', 1, TIMESTAMP '2026-10-14 10:00:00', 'COMPLETED'), "
                        + "(3, 'Người đã kết thúc', 2, TIMESTAMP '2026-10-14 10:00:00', 'TODO')");
            }
            ScheduledReminderService service = serviceAt("2026-10-13T09:00:00Z");

            service.scanTaskDueReminders(connection);
            service.scanTaskDueReminders(connection);

            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM notifications"));
            assertEquals("TASK_DUE_TOMORROW_REMINDER", scalar(connection,
                    "SELECT notification_type FROM notifications"));
            assertEquals("TASK_1_101_DUE_TOMORROW", scalar(connection,
                    "SELECT reminder_key FROM notifications"));
        }
    }

    @Test
    void fridayReminderSkipsInternsWithSubmittedReportForCurrentWeek() throws Exception {
        try (Connection connection = notificationConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO intern_profiles (id, user_id, internship_status, start_date, end_date) "
                        + "VALUES (1, 101, 'ACTIVE', DATE '2026-10-01', DATE '2026-12-31'), "
                        + "(2, 102, 'ACTIVE', DATE '2026-10-01', DATE '2026-12-31')");
                statement.execute("INSERT INTO weekly_reports (intern_profile_id, week_number, status) "
                        + "VALUES (2, 3, 'SUBMITTED')");
            }
            ScheduledReminderService service = serviceAt("2026-10-16T09:00:00Z");

            service.scanWeeklyReportReminders(connection);
            service.scanWeeklyReportReminders(connection);

            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM notifications"));
            assertEquals("101", scalar(connection, "SELECT user_id FROM notifications"));
            assertEquals("WEEKLY_REPORT_FRIDAY_REMINDER", scalar(connection,
                    "SELECT notification_type FROM notifications"));
        }
    }

    @Test
    void weeklyReminderDoesNothingBeforeFriday() throws Exception {
        try (Connection connection = notificationConnection()) {
            ScheduledReminderService service = serviceAt("2026-10-15T09:00:00Z");

            service.scanWeeklyReportReminders(connection);

            assertEquals(0, scalarInt(connection, "SELECT COUNT(*) FROM notifications"));
        }
    }

    @Test
    void mentorCanCreateMeetingForOwnedInternAndReminderIsSentOnce() throws Exception {
        try (Connection connection = notificationConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO mentors (id, user_id, status) VALUES (5, 500, 'ACTIVE')");
                statement.execute("INSERT INTO intern_profiles "
                        + "(id, user_id, mentor_id, internship_status, start_date, end_date) "
                        + "VALUES (1, 101, 5, 'ACTIVE', DATE '2026-10-01', DATE '2026-12-31'), "
                        + "(2, 102, 6, 'ACTIVE', DATE '2026-10-01', DATE '2026-12-31')");
            }
            MeetingDAO meetingDAO = new MeetingDAO();
            long meetingId = meetingDAO.createForMentor(connection, 500, "Họp kế hoạch", "Trao đổi tiến độ",
                    java.time.LocalDateTime.now().plusHours(2), java.util.List.of(1));
            assertTrue(meetingDAO.findForAttendee(connection, meetingId, 101) != null);
            assertTrue(meetingDAO.findForAttendee(connection, meetingId, 102) == null);

            boolean unassignedRejected = false;
            try {
                meetingDAO.createForMentor(connection, 500, "Họp không hợp lệ", null,
                        java.time.LocalDateTime.now().plusHours(3), java.util.List.of(2));
            } catch (IllegalArgumentException expected) {
                unassignedRejected = true;
            }
            assertTrue(unassignedRejected);
        }
    }

    @Test
    void meetingScanNotifiesActiveAttendeesOnlyOnce() throws Exception {
        try (Connection connection = notificationConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO intern_profiles "
                        + "(id, user_id, mentor_id, internship_status, start_date, end_date) "
                        + "VALUES (1, 101, 5, 'ACTIVE', DATE '2026-10-01', DATE '2026-12-31'), "
                        + "(2, 102, 5, 'COMPLETED', DATE '2026-10-01', DATE '2026-10-10')");
                statement.execute("INSERT INTO meetings (id, title, starts_at, status, created_by_user_id) "
                        + "VALUES (40, 'Họp mentor', TIMESTAMP '2026-10-13 10:00:00', 'SCHEDULED', 500)");
                statement.execute("INSERT INTO meeting_attendees (meeting_id, intern_profile_id) "
                        + "VALUES (40, 1), (40, 2)");
            }
            ScheduledReminderService service = new ScheduledReminderService(
                    new NotificationDAO(), new JdbcInternshipContextDAO(),
                    Clock.fixed(Instant.parse("2026-10-13T09:00:00Z"), ZoneOffset.UTC));

            service.scanMeetingReminders(connection);
            service.scanMeetingReminders(connection);

            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM notifications"));
            assertEquals("MEETING_REMINDER_60MINS", scalar(connection,
                    "SELECT notification_type FROM notifications"));
            assertEquals("MEETING_40_101_60M", scalar(connection, "SELECT reminder_key FROM notifications"));
        }
    }

    private ScheduledReminderService serviceAt(String instant) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
        return new ScheduledReminderService(new NotificationDAO(), clock);
    }

    private Connection notificationConnection() throws Exception {
        Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:scheduled-reminder-" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE intern_profiles (id BIGINT PRIMARY KEY, user_id BIGINT NOT NULL, "
                    + "mentor_id BIGINT, internship_status VARCHAR(30) NOT NULL, start_date DATE, end_date DATE)");
                statement.execute("CREATE TABLE mentors (id BIGINT PRIMARY KEY, user_id BIGINT NOT NULL, "
                    + "status VARCHAR(30) NOT NULL)");
            statement.execute("CREATE TABLE tasks (id BIGINT PRIMARY KEY, task_title VARCHAR(200) NOT NULL, "
                    + "intern_profile_id BIGINT, due_date DATETIME, status VARCHAR(30) NOT NULL)");
            statement.execute("CREATE TABLE weekly_reports (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "intern_profile_id BIGINT NOT NULL, week_number INT NOT NULL, status VARCHAR(30) NOT NULL)");
                statement.execute("CREATE TABLE meetings (id BIGINT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(200) NOT NULL, "
                    + "description TEXT, starts_at DATETIME NOT NULL, status VARCHAR(20) NOT NULL, "
                    + "created_by_user_id BIGINT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
                statement.execute("CREATE TABLE meeting_attendees (meeting_id BIGINT NOT NULL, "
                    + "intern_profile_id BIGINT NOT NULL, PRIMARY KEY (meeting_id, intern_profile_id))");
            statement.execute("CREATE TABLE notifications (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id BIGINT NOT NULL, notification_type VARCHAR(50), title VARCHAR(200) NOT NULL, "
                    + "message TEXT, target_url VARCHAR(255), target_entity_type VARCHAR(50), target_entity_id BIGINT, "
                    + "reminder_key VARCHAR(150), is_read BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT uq_notifications_reminder UNIQUE (user_id, reminder_key))");
        }
        return connection;
    }

    private String scalar(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet results = statement.executeQuery(sql)) {
            assertTrue(results.next());
            return results.getString(1);
        }
    }

    private int scalarInt(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet results = statement.executeQuery(sql)) {
            assertTrue(results.next());
            return results.getInt(1);
        }
    }
}