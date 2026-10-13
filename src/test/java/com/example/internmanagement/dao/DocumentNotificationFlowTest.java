package com.example.internmanagement.dao;

import com.example.internmanagement.model.InternDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentNotificationFlowTest {
    private Connection connection;
    private final InternDocumentDAO documentDAO = new InternDocumentDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE users(id BIGINT AUTO_INCREMENT PRIMARY KEY, status VARCHAR(20) NOT NULL)");
            statement.execute("CREATE TABLE roles(id BIGINT AUTO_INCREMENT PRIMARY KEY, role_code VARCHAR(20) NOT NULL)");
            statement.execute("CREATE TABLE user_roles(user_id BIGINT NOT NULL, role_id BIGINT NOT NULL)");
            statement.execute("CREATE TABLE intern_profiles(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL)");
            statement.execute("CREATE TABLE intern_documents(id BIGINT AUTO_INCREMENT PRIMARY KEY, intern_profile_id BIGINT NOT NULL, document_type VARCHAR(50) NOT NULL, file_name VARCHAR(255) NOT NULL, file_url VARCHAR(500) NOT NULL, file_size_bytes BIGINT, approval_status VARCHAR(20) NOT NULL, rejection_note TEXT, uploaded_at DATETIME)");
            statement.execute("CREATE TABLE notifications(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, title VARCHAR(200) NOT NULL, message TEXT, target_url VARCHAR(255), is_read BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("INSERT INTO users(id,status) VALUES(1,'ACTIVE'),(2,'ACTIVE')");
            statement.execute("INSERT INTO roles(id,role_code) VALUES(1,'HR')");
            statement.execute("INSERT INTO user_roles(user_id,role_id) VALUES(2,1)");
            statement.execute("INSERT INTO intern_profiles(id,user_id) VALUES(10,1)");
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
