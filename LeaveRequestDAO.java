package com.example.internmanagement.dao;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Nghiệp vụ nghỉ phép. Mọi phương thức nhận Connection do caller quản lý transaction
 * và ném IllegalArgumentException cho lỗi nghiệp vụ (thông điệp hiển thị được cho người dùng).
 */
public class LeaveRequestDAO {
    public static final Set<String> TYPES = Set.of("SICK_LEAVE", "PERSONAL_LEAVE", "UNPAID_LEAVE");
    public static final int MAX_WORKING_DAYS = 30;
    private final NotificationDAO notifications = new NotificationDAO();

    /** Thực tập sinh gửi đơn; trả về id đơn. */
    public long submit(Connection c, int userId, String type, LocalDate from, LocalDate to,
                       String reason, String ip, LocalDate today) throws SQLException {
        if (type == null || !TYPES.contains(type)) throw new IllegalArgumentException("Loại nghỉ không hợp lệ.");
        if (from == null || to == null) throw new IllegalArgumentException("Vui lòng chọn ngày nghỉ hợp lệ.");
        if (to.isBefore(from)) throw new IllegalArgumentException("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");

        boolean sick = "SICK_LEAVE".equals(type);
        if (from.isBefore(sick ? today : today.plusDays(1))) {
            throw new IllegalArgumentException(sick ? "Không thể gửi đơn cho ngày đã qua."
                    : "Đơn nghỉ cá nhân/không lương phải gửi trước ít nhất 1 ngày.");
        }
        reason = reason == null ? "" : reason.trim();
        if (reason.length() < 5) throw new IllegalArgumentException("Vui lòng nhập lý do ít nhất 5 ký tự.");
        if (reason.length() > 1000) throw new IllegalArgumentException("Lý do không vượt quá 1000 ký tự.");

        int days = workingDays(from, to);
        if (days == 0) throw new IllegalArgumentException("Khoảng thời gian chỉ gồm cuối tuần, không cần xin nghỉ.");
        if (days > MAX_WORKING_DAYS) throw new IllegalArgumentException("Mỗi đơn nghỉ tối đa " + MAX_WORKING_DAYS + " ngày làm việc.");

        long profileId; String fullName; String internCode;
        try (PreparedStatement s = c.prepareStatement(
                "SELECT ip.id,ip.intern_code,u.full_name FROM intern_profiles ip JOIN users u ON u.id=ip.user_id "
                        + "WHERE ip.user_id=? AND ip.internship_status='ACTIVE' FOR UPDATE")) {
            s.setInt(1, userId);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Tài khoản chưa có hồ sơ thực tập đang hoạt động.");
                profileId = r.getLong(1); internCode = r.getString(2); fullName = r.getString(3);
            }
        }
        try (PreparedStatement s = c.prepareStatement(
                "SELECT 1 FROM leave_requests WHERE intern_profile_id=? AND approval_status IN ('PENDING','APPROVED') "
                        + "AND start_date<=? AND end_date>=? LIMIT 1")) {
            s.setLong(1, profileId); s.setDate(2, Date.valueOf(to)); s.setDate(3, Date.valueOf(from));
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) throw new IllegalArgumentException("Khoảng thời gian này trùng với một đơn nghỉ đang chờ hoặc đã được duyệt.");
            }
        }
        long id;
        try (PreparedStatement s = c.prepareStatement(
                "INSERT INTO leave_requests(intern_profile_id,leave_type,start_date,end_date,total_days,reason) VALUES(?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            s.setLong(1, profileId); s.setString(2, type);
            s.setDate(3, Date.valueOf(from)); s.setDate(4, Date.valueOf(to));
            s.setInt(5, days); s.setString(6, reason);
            s.executeUpdate();
            try (ResultSet k = s.getGeneratedKeys()) { k.next(); id = k.getLong(1); }
        }
        notifications.notifyRole(c, "HR", "Đơn xin nghỉ phép mới",
                fullName + " (" + internCode + ") xin nghỉ " + typeLabel(type) + " từ " + from + " đến " + to
                        + " (" + days + " ngày làm việc).", "/attendance-report");
        audit(c, userId, "LEAVE_SUBMIT", id, "PENDING", ip);
        return id;
    }

    /** Thực tập sinh hủy đơn đang chờ của chính mình (xóa bản ghi, giữ audit log). */
    public void cancel(Connection c, int userId, long leaveId, String ip) throws SQLException {
        String fullName, internCode, from, to;
        try (PreparedStatement s = c.prepareStatement(
                "SELECT u.full_name,ip.intern_code,lr.start_date,lr.end_date FROM leave_requests lr "
                        + "JOIN intern_profiles ip ON ip.id=lr.intern_profile_id JOIN users u ON u.id=ip.user_id "
                        + "WHERE lr.id=? AND ip.user_id=? AND lr.approval_status='PENDING' FOR UPDATE")) {
            s.setLong(1, leaveId); s.setInt(2, userId);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Chỉ có thể hủy đơn đang chờ duyệt của chính bạn.");
                fullName = r.getString(1); internCode = r.getString(2); from = r.getString(3); to = r.getString(4);
            }
        }
        try (PreparedStatement s = c.prepareStatement("DELETE FROM leave_requests WHERE id=? AND approval_status='PENDING'")) {
            s.setLong(1, leaveId);
            if (s.executeUpdate() != 1) throw new IllegalArgumentException("Đơn nghỉ đã được xử lý.");
        }
        notifications.notifyRole(c, "HR", "Đơn nghỉ phép đã được hủy",
                fullName + " (" + internCode + ") đã hủy đơn nghỉ từ " + from + " đến " + to + ".", "/attendance-report");
        audit(c, userId, "LEAVE_CANCEL", leaveId, "CANCELLED", ip);
    }

    /** HR/Admin duyệt hoặc từ chối đơn đang chờ. */
    public void review(Connection c, int actorId, long leaveId, String decision, String note, String ip) throws SQLException {
        if (!"APPROVED".equals(decision) && !"REJECTED".equals(decision))
            throw new IllegalArgumentException("Kết quả duyệt không hợp lệ.");
        note = note == null ? "" : note.trim();
        if ("REJECTED".equals(decision) && note.length() < 5)
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối (ít nhất 5 ký tự).");

        int internUserId; String from, to;
        try (PreparedStatement s = c.prepareStatement(
                "SELECT ip.user_id,lr.start_date,lr.end_date FROM leave_requests lr "
                        + "JOIN intern_profiles ip ON ip.id=lr.intern_profile_id "
                        + "WHERE lr.id=? AND lr.approval_status='PENDING' FOR UPDATE")) {
            s.setLong(1, leaveId);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Đơn nghỉ đã được xử lý hoặc không tồn tại.");
                internUserId = r.getInt(1); from = r.getString(2); to = r.getString(3);
            }
        }
        try (PreparedStatement s = c.prepareStatement(
                "UPDATE leave_requests SET approval_status=?,approver_notes=? WHERE id=? AND approval_status='PENDING'")) {
            s.setString(1, decision); s.setString(2, note.isBlank() ? null : note); s.setLong(3, leaveId);
            if (s.executeUpdate() != 1) throw new IllegalArgumentException("Đơn nghỉ đã được xử lý.");
        }
        boolean ok = "APPROVED".equals(decision);
        try (PreparedStatement s = c.prepareStatement(
                "INSERT INTO notifications(user_id,title,message,target_url) VALUES(?,?,?,'/attendance')")) {
            s.setInt(1, internUserId);
            s.setString(2, ok ? "Đơn nghỉ phép đã được duyệt" : "Đơn nghỉ phép bị từ chối");
            s.setString(3, "Đơn nghỉ từ " + from + " đến " + to + (ok ? " đã được duyệt." : " đã bị từ chối.")
                    + (note.isBlank() ? "" : " Ghi chú: " + note));
            s.executeUpdate();
        }
        audit(c, actorId, ok ? "LEAVE_APPROVE" : "LEAVE_REJECT", leaveId, decision, ip);
    }

    public static int workingDays(LocalDate from, LocalDate to) {
        int n = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            DayOfWeek w = d.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) n++;
        }
        return n;
    }

    private void audit(Connection c, int userId, String action, long recordId, String status, String ip) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "INSERT INTO audit_logs(user_id,action_code,table_name,record_id,new_values_json,ip_address) "
                        + "VALUES(?,?,'leave_requests',?,?,?)")) {
            s.setInt(1, userId); s.setString(2, action); s.setLong(3, recordId);
            s.setString(4, "{\"status\":\"" + status + "\"}"); s.setString(5, ip);
            s.executeUpdate();
        }
    }

    private static String typeLabel(String t) {
        return "SICK_LEAVE".equals(t) ? "ốm" : "PERSONAL_LEAVE".equals(t) ? "cá nhân" : "không lương";
    }
}
