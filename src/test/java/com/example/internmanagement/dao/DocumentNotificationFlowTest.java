package com.example.internmanagement.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.internmanagement.model.InAppNotification;
import com.example.internmanagement.model.InternDocument;
import com.example.internmanagement.model.NotificationType;

class DocumentNotificationFlowTest {
    private Connection connection;
    private final InternDocumentDAO documentDAO = new InternDocumentDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final TaskDAO taskDAO = new TaskDAO();

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE users(id BIGINT AUTO_INCREMENT PRIMARY KEY, status VARCHAR(20) NOT NULL)");
            statement.execute("CREATE TABLE roles(id BIGINT AUTO_INCREMENT PRIMARY KEY, role_code VARCHAR(20) NOT NULL)");
            statement.execute("CREATE TABLE user_roles(user_id BIGINT NOT NULL, role_id BIGINT NOT NULL)");
            statement.execute("CREATE TABLE intern_profiles(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, mentor_id BIGINT NULL, internship_status VARCHAR(20) DEFAULT 'ACTIVE')");
            statement.execute("CREATE TABLE mentors(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE')");
            statement.execute("CREATE TABLE tasks(id BIGINT AUTO_INCREMENT PRIMARY KEY, task_title VARCHAR(200) NOT NULL, description TEXT, mentor_id BIGINT NULL, intern_profile_id BIGINT NULL, due_date DATETIME NULL, priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', completion_percent INT NOT NULL DEFAULT 0, status VARCHAR(30) NOT NULL DEFAULT 'TODO')");
            statement.execute("CREATE TABLE intern_documents(id BIGINT AUTO_INCREMENT PRIMARY KEY, intern_profile_id BIGINT NOT NULL, document_type VARCHAR(50) NOT NULL, file_name VARCHAR(255) NOT NULL, file_url VARCHAR(500) NOT NULL, file_size_bytes BIGINT, approval_status VARCHAR(20) NOT NULL, rejection_note TEXT, uploaded_at DATETIME)");
            statement.execute("CREATE TABLE notifications(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, notification_type VARCHAR(50), title VARCHAR(200) NOT NULL, message TEXT, target_url VARCHAR(255), target_entity_type VARCHAR(50), target_entity_id BIGINT, reminder_key VARCHAR(150), is_read BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT uq_notifications_reminder UNIQUE (user_id, reminder_key))");
            statement.execute("INSERT INTO users(id,status) VALUES(1,'ACTIVE'),(2,'ACTIVE'),(3,'ACTIVE')");
            statement.execute("INSERT INTO roles(id,role_code) VALUES(1,'HR'),(2,'MENTOR')");
            statement.execute("INSERT INTO user_roles(user_id,role_id) VALUES(2,1),(3,2)");
            statement.execute("INSERT INTO mentors(id,user_id,status) VALUES(10,3,'ACTIVE')");
            statement.execute("INSERT INTO intern_profiles(id,user_id,mentor_id,internship_status) VALUES(10,1,10,'ACTIVE')");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void uploadThenApproveNotifiesHrAndIntern() throws Exception {
        long documentId = submitDocument();

        assertNotification(2, "Có tài liệu mới cần duyệt", "/documents/review");
        assertTrue(documentDAO.review(connection, (int) documentId, "APPROVED", null, 2L));
        assertEquals(1, notificationDAO.notifyDocumentOwner(connection, (int) documentId,
                "Cập nhật tài liệu", "Tài liệu của bạn đã được duyệt.", "/home"));
        assertNotification(1, "Cập nhật tài liệu", "/home");
        assertEquals("APPROVED", scalar("SELECT approval_status FROM intern_documents WHERE id=" + documentId));
        assertFalse(documentDAO.review(connection, (int) documentId, "REJECTED", "trùng", 2L));
    }

    @Test
    void uploadThenRejectReturnsReviewNoteToIntern() throws Exception {
        long documentId = submitDocument();
        assertTrue(documentDAO.review(connection, (int) documentId, "REJECTED", "CV chưa có chữ ký", 2L));
        notificationDAO.notifyDocumentOwner(connection, (int) documentId,
                "Cập nhật tài liệu", "Tài liệu của bạn đã bị từ chối. Phản hồi: CV chưa có chữ ký", "/home");

        assertEquals("REJECTED", scalar("SELECT approval_status FROM intern_documents WHERE id=" + documentId));
        assertEquals("CV chưa có chữ ký", scalar("SELECT rejection_note FROM intern_documents WHERE id=" + documentId));
        assertEquals("Tài liệu của bạn đã bị từ chối. Phản hồi: CV chưa có chữ ký",
                scalar("SELECT message FROM notifications WHERE user_id=1"));
    }

    @Test
    void mentorAssignsTaskCreatesInternNotification() throws Exception {
        taskDAO.createTasksForInterns(connection,
                "Lịch trình thực tập",
                "Tham gia buổi hướng dẫn đầu tuần",
                LocalDate.now().plusDays(2),
                3,
                java.util.List.of(10));

        assertEquals("Nhiệm vụ mới", scalar("SELECT title FROM notifications WHERE user_id=1 ORDER BY id DESC LIMIT 1"));
        assertEquals("/home", scalar("SELECT target_url FROM notifications WHERE user_id=1 ORDER BY id DESC LIMIT 1"));
        assertEquals(1, notificationDAO.countUnreadForUser(connection, 1));
    }

    @Test
    void inAppNotificationPersistsEventMetadataAndReminderKeyOnce() throws Exception {
        InAppNotification notification = new InAppNotification();
        notification.setRecipientId(1);
        notification.setType(NotificationType.TASK_DUE_TOMORROW_REMINDER);
        notification.setTitle("Nhắc hạn nhiệm vụ");
        notification.setShortContent("Nhiệm vụ sắp đến hạn.");
        notification.setTargetUrl("/home");
        notification.setTargetEntityType("Task");
        notification.setTargetEntityId(42L);
        notification.setReminderKey("task-42-due-tomorrow");

        assertEquals(1, notificationDAO.notifyUser(connection, notification));
        assertEquals(0, notificationDAO.notifyUser(connection, notification));

        try (java.sql.PreparedStatement statement = connection.prepareStatement(
                "SELECT notification_type, message, target_entity_type, target_entity_id, reminder_key "
                        + "FROM notifications WHERE user_id = 1");
             ResultSet results = statement.executeQuery()) {
            assertTrue(results.next());
            assertEquals("TASK_DUE_TOMORROW_REMINDER", results.getString("notification_type"));
            assertEquals("Nhiệm vụ sắp đến hạn.", results.getString("message"));
            assertEquals("Task", results.getString("target_entity_type"));
            assertEquals(42L, results.getLong("target_entity_id"));
            assertEquals("task-42-due-tomorrow", results.getString("reminder_key"));
            assertFalse(results.next());
        }
    }

    private long submitDocument() throws Exception {
        InternDocument document = new InternDocument();
        document.setInternProfileId(10);
        document.setDocumentType("CV");
        document.setFileName("cv.pdf");
        document.setFileUrl("/uploads/test.pdf");
        document.setFileSizeBytes(100L);
        document.setUploadedAt(LocalDateTime.now());
        int documentId = documentDAO.add(connection, document);
        assertEquals(1, notificationDAO.notifyRole(connection, "HR", "Có tài liệu mới cần duyệt",
                "Thực tập sinh vừa nộp tài liệu cv.pdf.", "/documents/review"));
        return documentId;
    }

    private void assertNotification(int userId, String title, String targetUrl) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT title,target_url FROM notifications WHERE user_id=" + userId)) {
            assertTrue(results.next());
            assertEquals(title, results.getString(1));
            assertEquals(targetUrl, results.getString(2));
        }
    }

    private String scalar(String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet results = statement.executeQuery(sql)) {
            assertTrue(results.next());
            return results.getString(1);
        }
    }
}
