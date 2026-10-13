package com.example.internmanagement.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.NotificationType;

class NotificationEventServiceTest {
    @Test
    void singleEventSanitizesFinalEvaluationContent() throws Exception {
        try (Connection connection = notificationConnection()) {
            NotificationEventService service = new NotificationEventService(new NotificationDAO());

            service.processSingleEvent(connection, 11, NotificationType.FINAL_EVALUATION_COMPLETED,
                    "Đánh giá tổng kết", "Điểm của bạn là 9.5", "/home", "Evaluation", 73L);

            assertEquals("Đánh giá tổng kết của bạn đã sẵn sàng.",
                    scalar(connection, "SELECT message FROM notifications WHERE user_id = 11"));
            assertEquals("FINAL_EVALUATION_COMPLETED",
                    scalar(connection, "SELECT notification_type FROM notifications WHERE user_id = 11"));
            assertEquals("73", scalar(connection, "SELECT target_entity_id FROM notifications WHERE user_id = 11"));
        }
    }

    @Test
    void batchEventFormatsSingleAndMultipleItems() throws Exception {
        try (Connection connection = notificationConnection()) {
            NotificationEventService service = new NotificationEventService(new NotificationDAO());

            service.processBatchEvents(connection, 11, NotificationType.TASK_UNASSIGNED,
                    3, "nhiệm vụ đã được gỡ", "/home");

            assertEquals("3 nhiệm vụ đã được gỡ",
                    scalar(connection, "SELECT message FROM notifications WHERE user_id = 11"));
        }
    }

    @Test
    void admissionResultWaitsForEmailDispatch() throws Exception {
        try (Connection connection = notificationConnection()) {
            NotificationEventService service = new NotificationEventService(new NotificationDAO());

            service.processAdmissionResult(connection, 11, false);
            assertEquals(0, scalarInt(connection, "SELECT COUNT(*) FROM notifications"));

            service.processAdmissionResult(connection, 11, true);
            assertEquals(1, scalarInt(connection, "SELECT COUNT(*) FROM notifications"));
            assertEquals("ADMISSION_RESULT_RELEASED",
                    scalar(connection, "SELECT notification_type FROM notifications WHERE user_id = 11"));
        }
    }

    @Test
    void persistenceFailureDoesNotEscapeIntoBusinessFlow() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:event-service-failure;MODE=MySQL;DB_CLOSE_DELAY=-1")) {
            NotificationEventService service = new NotificationEventService(new NotificationDAO());
            Logger logger = Logger.getLogger(NotificationEventService.class.getName());
            Level previousLevel = logger.getLevel();
            logger.setLevel(Level.OFF);
            try {
                service.processAdmissionResult(connection, 11, true);
                assertTrue(!connection.isClosed());
            } finally {
                logger.setLevel(previousLevel);
            }
        }
    }

    private Connection notificationConnection() throws Exception {
        Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:event-service-" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Statement statement = connection.createStatement()) {
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