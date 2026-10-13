package com.example.internmanagement.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.internmanagement.dao.INotificationDAO;
import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.InAppNotification;
import com.example.internmanagement.model.NotificationType;

class InternNotificationServiceTest {
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final InternNotificationService service = new InternNotificationService(notificationDAO);

    @Test
    void listingIsPagedAndUnreadFilterIsScopedToCurrentUser() throws Exception {
        try (Connection connection = connection()) {
            insertNotification(connection, 10, "one", false, null, null);
            insertNotification(connection, 10, "two", true, null, null);
            insertNotification(connection, 10, "three", false, null, null);
            insertNotification(connection, 20, "other user", false, null, null);

            List<com.example.internmanagement.model.Notification> page =
                    service.getNotifications(connection, 10, true, 1, 1);

            assertEquals(1, page.size());
            assertEquals("one", page.get(0).getTitle());
            assertEquals("2", service.getUnreadBadgeDisplay(connection, 10));
        }
    }

    @Test
    void markReadCannotChangeAnotherUsersNotification() throws Exception {
        try (Connection connection = connection()) {
            long notificationId = insertNotification(connection, 20, "private", false, null, null);

            assertFalse(service.markAsRead(connection, 10, notificationId));
            assertEquals(false, isRead(connection, notificationId));
            assertTrue(service.markAsRead(connection, 20, notificationId));
            assertTrue(service.markAsRead(connection, 20, notificationId));
            assertTrue(isRead(connection, notificationId));
        }
    }

    @Test
    void clickMarksOwnedTaskNotificationReadAndReturnsSafeTarget() throws Exception {
        try (Connection connection = connection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO intern_profiles (id, user_id, internship_status) "
                        + "VALUES (1, 10, 'ACTIVE'), (2, 20, 'ACTIVE')");
                statement.execute("INSERT INTO tasks (id, task_title, intern_profile_id, status) "
                        + "VALUES (50, 'Task A', 1, 'TODO'), (51, 'Task B', 2, 'TODO'), "
                        + "(52, 'Completed', 1, 'COMPLETED')");
            }
            long assignedTaskNotice = insertNotification(connection, 10, "task", false, "Task", 50L);
            long foreignTaskNotice = insertNotification(connection, 10, "foreign task", false, "Task", 51L);
            long completedTaskNotice = insertNotification(connection, 10, "completed task", false, "Task", 52L);
            long externalLinkNotice = insertNotification(connection, 10, "external", false, null, null);
            setTargetUrl(connection, externalLinkNotice, "https://example.com");

            InternNotificationService.ClickResult valid =
                    service.handleClickAndNavigate(connection, 10, assignedTaskNotice);
            InternNotificationService.ClickResult foreign =
                    service.handleClickAndNavigate(connection, 10, foreignTaskNotice);
            InternNotificationService.ClickResult completed =
                    service.handleClickAndNavigate(connection, 10, completedTaskNotice);
            InternNotificationService.ClickResult external =
                    service.handleClickAndNavigate(connection, 10, externalLinkNotice);

            assertTrue(valid.success());
            assertEquals("/home", valid.targetUrl());
            assertFalse(foreign.success());
            assertFalse(completed.success());
            assertFalse(external.success());
            assertNull(foreign.targetUrl());
            assertTrue(isRead(connection, assignedTaskNotice));
        }
    }

    @Test
    void badgeCapsAtNinetyNinePlus() throws Exception {
        try (Connection connection = connection()) {
            for (int index = 0; index < 100; index++) {
                insertNotification(connection, 10, "item " + index, false, null, null);
            }

            assertEquals("99+", service.getUnreadBadgeDisplay(connection, 10));
        }
    }

    @Test
    void daoContractSupportsCrudReminderLookupAndRetention() throws Exception {
        try (Connection connection = connection()) {
            INotificationDAO dao = new NotificationDAO();
            InAppNotification notification = new InAppNotification();
            notification.setRecipientId(10);
            notification.setType(NotificationType.TASK_ASSIGNED);
            notification.setTitle("Task mới");
            notification.setShortContent("Có task mới.");
            notification.setReminderKey("task-assigned-80");
            notification.setCreatedAt(LocalDateTime.now().minusDays(90));

            assertEquals(1, dao.insert(connection, notification));
            InAppNotification loaded = dao.findById(connection, notification.getId());
            assertEquals(notification.getId(), loaded.getId());
            assertTrue(dao.existsByReminderKey(connection, 10, "task-assigned-80"));

            loaded.setRead(true);
            assertEquals(1, dao.update(connection, loaded));
            assertTrue(dao.findByIdForRecipient(connection, 10, loaded.getId()).isRead());
            assertEquals(1, dao.deleteOlderThan(connection, LocalDateTime.now().minusDays(30)));
            assertNull(dao.findById(connection, loaded.getId()));
        }
    }

    private Connection connection() throws Exception {
        Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:intern-notifications-" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE notifications (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id BIGINT NOT NULL, notification_type VARCHAR(50), title VARCHAR(200) NOT NULL, "
                    + "message TEXT, target_url VARCHAR(255), target_entity_type VARCHAR(50), target_entity_id BIGINT, "
                    + "reminder_key VARCHAR(150), is_read BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT uq_notifications_reminder UNIQUE (user_id, reminder_key))");
            statement.execute("CREATE TABLE intern_profiles (id BIGINT PRIMARY KEY, user_id BIGINT NOT NULL, "
                    + "internship_status VARCHAR(30) NOT NULL)");
            statement.execute("CREATE TABLE tasks (id BIGINT PRIMARY KEY, task_title VARCHAR(200) NOT NULL, "
                    + "intern_profile_id BIGINT NOT NULL, status VARCHAR(30) NOT NULL)");
        }
        return connection;
    }

    private long insertNotification(Connection connection, int userId, String title, boolean read,
                                    String targetType, Long targetId) throws Exception {
        try (var statement = connection.prepareStatement(
                "INSERT INTO notifications (user_id, notification_type, title, message, target_url, "
                        + "target_entity_type, target_entity_id, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, NotificationType.TASK_ASSIGNED.name());
            statement.setString(3, title);
            statement.setString(4, title);
            statement.setString(5, "/home");
            statement.setString(6, targetType);
            if (targetId == null) {
                statement.setNull(7, java.sql.Types.BIGINT);
            } else {
                statement.setLong(7, targetId);
            }
            statement.setBoolean(8, read);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                return keys.getLong(1);
            }
        }
    }

    private boolean isRead(Connection connection, long notificationId) throws Exception {
        try (var statement = connection.prepareStatement("SELECT is_read FROM notifications WHERE id = ?")) {
            statement.setLong(1, notificationId);
            try (ResultSet results = statement.executeQuery()) {
                assertTrue(results.next());
                return results.getBoolean(1);
            }
        }
    }

    private void setTargetUrl(Connection connection, long notificationId, String targetUrl) throws Exception {
        try (var statement = connection.prepareStatement("UPDATE notifications SET target_url = ? WHERE id = ?")) {
            statement.setString(1, targetUrl);
            statement.setLong(2, notificationId);
            statement.executeUpdate();
        }
    }
}