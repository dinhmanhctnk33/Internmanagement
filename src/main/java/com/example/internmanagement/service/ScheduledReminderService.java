package com.example.internmanagement.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.internmanagement.dao.IInternshipContextDAO;
import com.example.internmanagement.dao.JdbcInternshipContextDAO;
import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.InAppNotification;
import com.example.internmanagement.model.NotificationType;
import com.example.internmanagement.util.DBConnection;

public class ScheduledReminderService {
    private static final Logger LOGGER = Logger.getLogger(ScheduledReminderService.class.getName());

    private final NotificationDAO notificationDAO;
    private final IInternshipContextDAO contextDAO;
    private final Clock clock;

    public ScheduledReminderService(NotificationDAO notificationDAO) {
        this(notificationDAO, new JdbcInternshipContextDAO(), Clock.systemDefaultZone());
    }

    public ScheduledReminderService(NotificationDAO notificationDAO, Clock clock) {
        this(notificationDAO, new JdbcInternshipContextDAO(), clock);
    }

    public ScheduledReminderService(NotificationDAO notificationDAO, IInternshipContextDAO contextDAO, Clock clock) {
        this.notificationDAO = notificationDAO;
        this.contextDAO = contextDAO;
        this.clock = clock;
    }

    public void scanMeetingReminders() {
        try (Connection connection = DBConnection.getConnection()) {
            scanMeetingReminders(connection);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Không thể quét nhắc lịch họp", exception);
        }
    }

    public void scanMeetingReminders(Connection connection) throws SQLException {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.plusMinutes(50);
        LocalDateTime end = now.plusMinutes(65);
        for (IInternshipContextDAO.MeetingDto meeting : contextDAO.getActiveMeetingsBetween(connection, start, end)) {
            for (Integer recipientId : contextDAO.getInternAttendeesForMeeting(connection, meeting.id())) {
                InAppNotification notification = new InAppNotification();
                notification.setRecipientId(recipientId);
                notification.setType(NotificationType.MEETING_REMINDER_60MINS);
                notification.setTitle("Nhắc lịch họp sắp diễn ra");
                notification.setShortContent("Cuộc họp '" + meeting.title() + "' sẽ bắt đầu sau 60 phút nữa.");
                notification.setTargetUrl("/api/meetings?id=" + meeting.id());
                notification.setTargetEntityType("Meeting");
                notification.setTargetEntityId(meeting.id());
                notification.setReminderKey("MEETING_" + meeting.id() + "_" + recipientId + "_60M");
                notificationDAO.notifyUser(connection, notification);
            }
        }
    }

    public void scanTaskDueReminders() {
        try (Connection connection = DBConnection.getConnection()) {
            scanTaskDueReminders(connection);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Không thể quét nhắc hạn nhiệm vụ", exception);
        }
    }

    public void scanTaskDueReminders(Connection connection) throws SQLException {
        LocalDate today = LocalDate.now(clock);
        LocalDate tomorrow = today.plusDays(1);
        for (IInternshipContextDAO.TaskDto task : contextDAO.getIncompleteTasksDueOn(connection, tomorrow)) {
            InAppNotification notification = new InAppNotification();
            notification.setRecipientId(task.assigneeId());
            notification.setType(NotificationType.TASK_DUE_TOMORROW_REMINDER);
            notification.setTitle("Nhắc hạn hoàn thành nhiệm vụ");
            notification.setShortContent("Nhiệm vụ '" + task.title() + "' sẽ đến hạn vào ngày mai.");
            notification.setTargetUrl("/home");
            notification.setTargetEntityType("Task");
            notification.setTargetEntityId(task.id());
            notification.setReminderKey("TASK_" + task.id() + "_" + task.assigneeId() + "_DUE_TOMORROW");
            notificationDAO.notifyUser(connection, notification);
        }
    }

    public void scanWeeklyReportReminders() {
        try (Connection connection = DBConnection.getConnection()) {
            scanWeeklyReportReminders(connection);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Không thể quét nhắc nộp báo cáo tuần", exception);
        }
    }

    public void scanWeeklyReportReminders(Connection connection) throws SQLException {
        LocalDate today = LocalDate.now(clock);
        if (today.getDayOfWeek() != DayOfWeek.FRIDAY) {
            return;
        }

        for (IInternshipContextDAO.WeeklyReportReminderDto intern
                : contextDAO.getInternsMissingWeeklyReport(connection, today)) {
            InAppNotification notification = new InAppNotification();
            notification.setRecipientId(intern.recipientId());
            notification.setType(NotificationType.WEEKLY_REPORT_FRIDAY_REMINDER);
            notification.setTitle("Nhắc nộp báo cáo tuần");
            notification.setShortContent("Hôm nay là Thứ Sáu, đừng quên hoàn thiện và nộp báo cáo tuần trước Chủ Nhật.");
            notification.setTargetUrl("/home");
            notification.setTargetEntityType("WeeklyReport");
            notification.setReminderKey("REPORT_WEEK_" + intern.weekNumber() + "_"
                    + intern.recipientId() + "_FRIDAY");
            notificationDAO.notifyUser(connection, notification);
        }
    }
}