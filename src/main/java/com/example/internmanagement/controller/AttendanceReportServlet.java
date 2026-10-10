package com.example.internmanagement.controller;

import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/attendance-report")
public class AttendanceReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isHrOrAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = parseDate(request.getParameter("from"), today.withDayOfMonth(1));
            toDate = parseDate(request.getParameter("to"), today);
            if (toDate.isBefore(fromDate)) {
                throw new IllegalArgumentException("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
            }
            if (ChronoUnit.DAYS.between(fromDate, toDate) > 366) {
                throw new IllegalArgumentException("Khoảng báo cáo không được vượt quá 366 ngày.");
            }
        } catch (IllegalArgumentException exception) {
            request.setAttribute("errorMessage", exception.getMessage());
            fromDate = today.withDayOfMonth(1);
            toDate = today;
        }

        Long programId = parseId(request.getParameter("programId"));
        String keyword = clean(request.getParameter("q"));

        try (Connection connection = DBConnection.getConnection()) {
            List<Map<String, Object>> report = loadSummary(connection, fromDate, toDate, programId, keyword);
            List<Map<String, Object>> attendance = loadAttendance(connection, fromDate, toDate, programId, keyword);
            List<Map<String, Object>> leaves = loadLeaves(connection, fromDate, toDate, programId, keyword);

            request.setAttribute("report", report);
            request.setAttribute("attendanceRecords", attendance);
            request.setAttribute("leaveRequests", leaves);
            request.setAttribute("programs", loadPrograms(connection));
            request.setAttribute("metrics", calculateMetrics(report));
            request.setAttribute("fromDate", fromDate);
            request.setAttribute("toDate", toDate);
            request.setAttribute("selectedProgramId", programId);
            request.setAttribute("keyword", keyword);
            request.setAttribute("currentUser", request.getSession().getAttribute("user"));
            request.getRequestDispatcher("/WEB-INF/views/attendance-report.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải báo cáo chuyên cần.", exception);
        }
    }

    private List<Map<String, Object>> loadSummary(Connection connection, LocalDate from, LocalDate to,
                                                   Long programId, String keyword) throws SQLException {
        String sql = "SELECT ip.id,u.full_name,ip.intern_code,p.program_name,d.dept_name," +
                "(SELECT COUNT(*) FROM attendance_records ar WHERE ar.intern_profile_id=ip.id " +
                "AND ar.work_date BETWEEN ? AND ? AND ar.check_in_time IS NOT NULL) work_days," +
                "(SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE,ar.check_in_time,ar.check_out_time)),0) " +
                "FROM attendance_records ar WHERE ar.intern_profile_id=ip.id AND ar.work_date BETWEEN ? AND ? " +
                "AND ar.check_in_time IS NOT NULL AND ar.check_out_time IS NOT NULL) total_minutes," +
                "(SELECT COUNT(*) FROM attendance_records ar WHERE ar.intern_profile_id=ip.id " +
                "AND ar.work_date BETWEEN ? AND ? AND ar.status='LATE') late_days," +
                "(SELECT COUNT(*) FROM attendance_records ar WHERE ar.intern_profile_id=ip.id " +
                "AND ar.work_date BETWEEN ? AND ? AND ar.status='EARLY_LEAVE') early_leave_days," +
                "(SELECT COUNT(*) FROM attendance_records ar WHERE ar.intern_profile_id=ip.id " +
                "AND ar.work_date BETWEEN ? AND ? AND ar.check_in_time IS NOT NULL AND ar.check_out_time IS NULL " +
                "AND ar.work_date<CURDATE()) missing_checkout," +
                "(SELECT COALESCE(SUM(GREATEST(0,DATEDIFF(LEAST(lr.end_date,?),GREATEST(lr.start_date,?))+1)),0) " +
                "FROM leave_requests lr WHERE lr.intern_profile_id=ip.id AND lr.approval_status='APPROVED' " +
                "AND lr.end_date>=? AND lr.start_date<=?) approved_leave_days " +
                "FROM intern_profiles ip JOIN users u ON u.id=ip.user_id " +
                "LEFT JOIN internship_programs p ON p.id=ip.program_id " +
                "LEFT JOIN departments d ON d.id=p.department_id WHERE ip.internship_status='ACTIVE' " +
                "AND (? IS NULL OR ip.program_id=?) AND (?='' OR u.full_name LIKE CONCAT('%',?,'%') " +
                "OR ip.intern_code LIKE CONCAT('%',?,'%')) ORDER BY u.full_name";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (int pair = 0; pair < 5; pair++) {
                statement.setDate(index++, Date.valueOf(from));
                statement.setDate(index++, Date.valueOf(to));
            }
            statement.setDate(index++, Date.valueOf(to));
            statement.setDate(index++, Date.valueOf(from));
            statement.setDate(index++, Date.valueOf(from));
            statement.setDate(index++, Date.valueOf(to));
            setNullableLong(statement, index++, programId);
            setNullableLong(statement, index++, programId);
            statement.setString(index++, keyword);
            statement.setString(index++, keyword);
            statement.setString(index, keyword);
            return rows(statement);
        }
    }

    private List<Map<String, Object>> loadAttendance(Connection connection, LocalDate from, LocalDate to,
                                                      Long programId, String keyword) throws SQLException {
        String sql = "SELECT ar.work_date,u.full_name,ip.intern_code,p.program_name,ar.check_in_time,ar.check_out_time," +
                "TIME_FORMAT(ar.check_in_time,'%H:%i') check_in_display,TIME_FORMAT(ar.check_out_time,'%H:%i') check_out_display," +
                "CASE WHEN ar.check_in_time IS NOT NULL AND ar.check_out_time IS NOT NULL " +
                "THEN TIMESTAMPDIFF(MINUTE,ar.check_in_time,ar.check_out_time) END total_minutes,ar.status " +
                "FROM attendance_records ar JOIN intern_profiles ip ON ip.id=ar.intern_profile_id " +
                "JOIN users u ON u.id=ip.user_id LEFT JOIN internship_programs p ON p.id=ip.program_id " +
                "WHERE ar.work_date BETWEEN ? AND ? AND (? IS NULL OR ip.program_id=?) " +
                "AND (?='' OR u.full_name LIKE CONCAT('%',?,'%') OR ip.intern_code LIKE CONCAT('%',?,'%')) " +
                "ORDER BY ar.work_date DESC,u.full_name LIMIT 300";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindCommon(statement, from, to, programId, keyword);
            return rows(statement);
        }
    }

    private List<Map<String, Object>> loadLeaves(Connection connection, LocalDate from, LocalDate to,
                                                  Long programId, String keyword) throws SQLException {
        String sql = "SELECT lr.id,u.full_name,ip.intern_code,p.program_name,lr.leave_type,lr.start_date,lr.end_date," +
                "lr.total_days,lr.reason,lr.approval_status,lr.approver_notes FROM leave_requests lr " +
                "JOIN intern_profiles ip ON ip.id=lr.intern_profile_id JOIN users u ON u.id=ip.user_id " +
                "LEFT JOIN internship_programs p ON p.id=ip.program_id WHERE lr.end_date>=? AND lr.start_date<=? " +
                "AND (? IS NULL OR ip.program_id=?) AND (?='' OR u.full_name LIKE CONCAT('%',?,'%') " +
                "OR ip.intern_code LIKE CONCAT('%',?,'%')) ORDER BY lr.start_date DESC,lr.id DESC LIMIT 200";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindCommon(statement, from, to, programId, keyword);
            return rows(statement);
        }
    }

    private void bindCommon(PreparedStatement statement, LocalDate from, LocalDate to,
                            Long programId, String keyword) throws SQLException {
        statement.setDate(1, Date.valueOf(from));
        statement.setDate(2, Date.valueOf(to));
        setNullableLong(statement, 3, programId);
        setNullableLong(statement, 4, programId);
        statement.setString(5, keyword);
        statement.setString(6, keyword);
        statement.setString(7, keyword);
    }

    private List<Map<String, Object>> loadPrograms(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id,program_code,program_name FROM internship_programs ORDER BY program_name")) {
            return rows(statement);
        }
    }

    private Map<String, Object> calculateMetrics(List<Map<String, Object>> report) {
        long workDays = 0, minutes = 0, lateDays = 0, missingCheckout = 0;
        double leaveDays = 0;
        for (Map<String, Object> row : report) {
            workDays += number(row.get("work_days")).longValue();
            minutes += number(row.get("total_minutes")).longValue();
            lateDays += number(row.get("late_days")).longValue();
            missingCheckout += number(row.get("missing_checkout")).longValue();
            leaveDays += number(row.get("approved_leave_days")).doubleValue();
            row.put("total_hours", String.format(java.util.Locale.US, "%.1f", number(row.get("total_minutes")).doubleValue() / 60));
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("interns", report.size());
        metrics.put("workDays", workDays);
        metrics.put("totalHours", String.format(java.util.Locale.US, "%.1f", minutes / 60.0));
        metrics.put("lateDays", lateDays);
        metrics.put("missingCheckout", missingCheckout);
        metrics.put("leaveDays", String.format(java.util.Locale.US, "%.1f", leaveDays));
        return metrics;
    }

    private List<Map<String, Object>> rows(PreparedStatement statement) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            int count = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int index = 1; index <= count; index++) {
                    row.put(rs.getMetaData().getColumnLabel(index), rs.getObject(index));
                }
                result.add(row);
            }
        }
        return result;
    }

    private Number number(Object value) {
        return value instanceof Number number ? number : 0;
    }

    private void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.BIGINT);
        else statement.setLong(index, value);
    }

    private LocalDate parseDate(String value, LocalDate fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return LocalDate.parse(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Ngày báo cáo không hợp lệ.");
        }
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            long id = Long.parseLong(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isHrOrAdmin(HttpServletRequest request) {
        String role = String.valueOf(request.getSession().getAttribute("userRole"));
        return "HR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }
}
