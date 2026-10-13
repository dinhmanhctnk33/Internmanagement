package com.example.internmanagement.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LeaveRequestFlowTest {
    // Thứ Hai cố định để kết quả test không phụ thuộc ngày chạy.
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 12);
    private Connection c;
    private final LeaveRequestDAO dao = new LeaveRequestDAO();

    @BeforeEach
    void setUp() throws Exception {
        c = DriverManager.getConnection("jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Statement s = c.createStatement()) {
            s.execute("CREATE TABLE users(id BIGINT AUTO_INCREMENT PRIMARY KEY, full_name VARCHAR(100), status VARCHAR(20) NOT NULL)");
            s.execute("CREATE TABLE roles(id BIGINT AUTO_INCREMENT PRIMARY KEY, role_code VARCHAR(20) NOT NULL)");
            s.execute("CREATE TABLE user_roles(user_id BIGINT NOT NULL, role_id BIGINT NOT NULL)");
            s.execute("CREATE TABLE intern_profiles(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, intern_code VARCHAR(30), internship_status VARCHAR(30) NOT NULL)");
            s.execute("CREATE TABLE leave_requests(id BIGINT AUTO_INCREMENT PRIMARY KEY, intern_profile_id BIGINT NOT NULL, leave_type VARCHAR(50) NOT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL, total_days DECIMAL(3,1) NOT NULL, reason TEXT, approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', approver_notes TEXT)");
            s.execute("CREATE TABLE notifications(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, title VARCHAR(200) NOT NULL, message TEXT, target_url VARCHAR(255), is_read BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            s.execute("CREATE TABLE audit_logs(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT, action_code VARCHAR(50) NOT NULL, table_name VARCHAR(50) NOT NULL, record_id BIGINT, new_values_json VARCHAR(500), ip_address VARCHAR(45))");
            s.execute("INSERT INTO users(id,full_name,status) VALUES(1,'Intern A','ACTIVE'),(2,'HR One','ACTIVE'),(3,'Intern B','ACTIVE')");
            s.execute("INSERT INTO roles(id,role_code) VALUES(1,'HR'),(2,'INTERN')");
            s.execute("INSERT INTO user_roles(user_id,role_id) VALUES(2,1),(1,2),(3,2)");
            s.execute("INSERT INTO intern_profiles(id,user_id,intern_code,internship_status) VALUES(10,1,'TTS1','ACTIVE'),(11,3,'TTS2','ACTIVE')");
        }
    }

    @AfterEach
    void tearDown() throws Exception { c.close(); }

    @Test
    void submitStoresWorkingDaysAndNotifiesHr() throws Exception {
        // Thứ Ba 13/10 -> Thứ Sáu 16/10 = 4 ngày làm việc
        long id = dao.submit(c, 1, "PERSONAL_LEAVE", d(13), d(16), "Việc gia đình", "127.0.0.1", TODAY);
        assertEquals("4.0", scalar("SELECT total_days FROM leave_requests WHERE id=" + id));
        assertEquals("PENDING", scalar("SELECT approval_status FROM leave_requests WHERE id=" + id));
        assertEquals("Đơn xin nghỉ phép mới", scalar("SELECT title FROM notifications WHERE user_id=2"));
        assertEquals("LEAVE_SUBMIT", scalar("SELECT action_code FROM audit_logs WHERE record_id=" + id));
    }

    @Test
    void weekendOnlyRangeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> dao.submit(c, 1, "PERSONAL_LEAVE", d(17), d(18), "Cuối tuần", "ip", TODAY));
    }

    @Test
    void advanceNoticeRules() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> dao.submit(c, 1, "PERSONAL_LEAVE", TODAY, TODAY, "Nghỉ trong ngày", "ip", TODAY));
        dao.submit(c, 1, "SICK_LEAVE", TODAY, TODAY, "Bị sốt cao", "ip", TODAY); // nghỉ ốm được báo trong ngày
        assertThrows(IllegalArgumentException.class,
                () -> dao.submit(c, 1, "SICK_LEAVE", d(11), d(11), "Đã qua rồi", "ip", TODAY));
    }

    @Test
    void invalidInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> dao.submit(c, 1, "HOLIDAY", d(13), d(13), "Lý do hợp lệ", "ip", TODAY));
        assertThrows(IllegalArgumentException.class, () -> dao.submit(c, 1, "SICK_LEAVE", d(14), d(13), "Lý do hợp lệ", "ip", TODAY));
        assertThrows(IllegalArgumentException.class, () -> dao.submit(c, 1, "SICK_LEAVE", d(13), d(13), "ốm", "ip", TODAY));
        assertThrows(IllegalArgumentException.class, () -> dao.submit(c, 1, "UNPAID_LEAVE", d(13), d(30).plusMonths(2), "Nghỉ quá dài", "ip", TODAY));
    }

    @Test
    void overlappingRequestIsRejectedButOtherInternIsNot() throws Exception {
        dao.submit(c, 1, "PERSONAL_LEAVE", d(14), d(15), "Đơn đầu tiên", "ip", TODAY);
        assertThrows(IllegalArgumentException.class,
                () -> dao.submit(c, 1, "SICK_LEAVE", d(15), d(16), "Trùng ngày 15", "ip", TODAY));
        dao.submit(c, 3, "PERSONAL_LEAVE", d(14), d(15), "TTS khác cùng ngày", "ip", TODAY);
    }

    @Test
    void internCanCancelOnlyOwnPendingRequest() throws Exception {
        long id = dao.submit(c, 1, "PERSONAL_LEAVE", d(14), d(14), "Sẽ hủy đơn này", "ip", TODAY);
        assertThrows(IllegalArgumentException.class, () -> dao.cancel(c, 3, id, "ip")); // TTS khác
        dao.cancel(c, 1, id, "ip");
        assertEquals("0", scalar("SELECT COUNT(*) FROM leave_requests"));
        assertEquals("2", scalar("SELECT COUNT(*) FROM notifications WHERE user_id=2")); // gửi + hủy
    }

    @Test
    void approveNotifiesInternAndCannotBeReviewedTwice() throws Exception {
        long id = dao.submit(c, 1, "PERSONAL_LEAVE", d(14), d(14), "Việc cá nhân", "ip", TODAY);
        dao.review(c, 2, id, "APPROVED", "", "ip");
        assertEquals("APPROVED", scalar("SELECT approval_status FROM leave_requests WHERE id=" + id));
        assertEquals("Đơn nghỉ phép đã được duyệt", scalar("SELECT title FROM notifications WHERE user_id=1"));
        assertThrows(IllegalArgumentException.class, () -> dao.review(c, 2, id, "REJECTED", "Đổi ý rồi", "ip"));
        // đơn đã duyệt không còn hủy được
        assertThrows(IllegalArgumentException.class, () -> dao.cancel(c, 1, id, "ip"));
    }

    @Test
    void rejectRequiresReasonAndPassesItToIntern() throws Exception {
        long id = dao.submit(c, 1, "UNPAID_LEAVE", d(14), d(14), "Việc riêng", "ip", TODAY);
        assertThrows(IllegalArgumentException.class, () -> dao.review(c, 2, id, "REJECTED", " ", "ip"));
        dao.review(c, 2, id, "REJECTED", "Trùng đợt bảo vệ dự án", "ip");
        assertEquals("Trùng đợt bảo vệ dự án", scalar("SELECT approver_notes FROM leave_requests WHERE id=" + id));
        assertTrue(scalar("SELECT message FROM notifications WHERE user_id=1").contains("Trùng đợt bảo vệ dự án"));
        // đơn bị từ chối không chặn gửi lại cùng ngày
        dao.submit(c, 1, "UNPAID_LEAVE", d(14), d(14), "Gửi lại đơn", "ip", TODAY);
    }

    private static LocalDate d(int day) { return LocalDate.of(2026, 10, day); }

    private String scalar(String sql) throws Exception {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            assertTrue(r.next());
            return r.getString(1);
        }
    }
}
