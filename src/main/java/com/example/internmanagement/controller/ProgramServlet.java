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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/programs")
public class ProgramServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isHrOrAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try (Connection connection = DBConnection.getConnection()) {
            request.setAttribute("programs", loadPrograms(connection));
            request.setAttribute("departments", loadDepartments(connection));
            request.setAttribute("currentUser", request.getSession().getAttribute("user"));
            request.getRequestDispatcher("/WEB-INF/views/program-management.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải danh sách chương trình thực tập.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isHrOrAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        String action = value(request.getParameter("action"));
        try (Connection connection = DBConnection.getConnection()) {
            if ("update-dates".equals(action)) {
                updateDates(request, connection);
                response.sendRedirect(request.getContextPath() + "/programs?success=updated");
            } else {
                createProgram(request, connection);
                response.sendRedirect(request.getContextPath() + "/programs?success=created");
            }
        } catch (IllegalArgumentException exception) {
            response.sendRedirect(request.getContextPath() + "/programs?error="
                    + java.net.URLEncoder.encode(exception.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        } catch (SQLException exception) {
            String message = exception.getErrorCode() == 1062
                    ? "Mã chương trình đã tồn tại."
                    : "Không thể lưu chương trình. Vui lòng thử lại.";
            response.sendRedirect(request.getContextPath() + "/programs?error="
                    + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private void createProgram(HttpServletRequest request, Connection connection) throws SQLException {
        String code = required(request, "code", "Vui lòng nhập mã chương trình.");
        String name = required(request, "name", "Vui lòng nhập tên chương trình.");
        long departmentId = positiveLong(request, "departmentId", "Vui lòng chọn phòng ban.");
        int quota = positiveInt(request, "quota", "Chỉ tiêu phải lớn hơn 0.");
        LocalDate startDate = date(request, "startDate", "Vui lòng chọn ngày bắt đầu.");
        LocalDate endDate = date(request, "endDate", "Vui lòng chọn ngày kết thúc.");
        validatePeriod(startDate, endDate);
        String description = value(request.getParameter("description"));
        String status = normaliseStatus(request.getParameter("status"));

        String sql = "INSERT INTO internship_programs "
                + "(program_code,program_name,department_id,quota_count,start_date,end_date,description,status) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setLong(3, departmentId);
            statement.setInt(4, quota);
            statement.setDate(5, Date.valueOf(startDate));
            statement.setDate(6, Date.valueOf(endDate));
            statement.setString(7, description.isBlank() ? null : description);
            statement.setString(8, status);
            statement.executeUpdate();
        }
    }

    private void updateDates(HttpServletRequest request, Connection connection) throws SQLException {
        long programId = positiveLong(request, "programId", "Chương trình không hợp lệ.");
        LocalDate startDate = date(request, "startDate", "Vui lòng chọn ngày bắt đầu.");
        LocalDate endDate = date(request, "endDate", "Vui lòng chọn ngày kết thúc.");
        validatePeriod(startDate, endDate);
        String status = normaliseStatus(request.getParameter("status"));
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE internship_programs SET start_date=?,end_date=?,status=? WHERE id=?")) {
            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));
            statement.setString(3, status);
            statement.setLong(4, programId);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy chương trình cần cập nhật.");
            }
        }
    }

    private List<Map<String, Object>> loadPrograms(Connection connection) throws SQLException {
        String sql = "SELECT p.id,p.program_code,p.program_name,p.quota_count,p.start_date,p.end_date,"
                + "p.description,p.status,d.dept_name,COUNT(ip.id) intern_count "
                + "FROM internship_programs p LEFT JOIN departments d ON d.id=p.department_id "
                + "LEFT JOIN intern_profiles ip ON ip.program_id=p.id "
                + "GROUP BY p.id,p.program_code,p.program_name,p.quota_count,p.start_date,p.end_date,"
                + "p.description,p.status,d.dept_name ORDER BY p.start_date DESC,p.id DESC";
        return query(connection, sql);
    }

    private List<Map<String, Object>> loadDepartments(Connection connection) throws SQLException {
        return query(connection, "SELECT id,dept_code,dept_name FROM departments WHERE is_active=TRUE ORDER BY dept_name");
    }

    private List<Map<String, Object>> query(Connection connection, String sql) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            int columns = result.getMetaData().getColumnCount();
            while (result.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int index = 1; index <= columns; index++) {
                    row.put(result.getMetaData().getColumnLabel(index), result.getObject(index));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private boolean isHrOrAdmin(HttpServletRequest request) {
        String role = String.valueOf(request.getSession().getAttribute("userRole"));
        return "HR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    private String required(HttpServletRequest request, String name, String message) {
        String result = value(request.getParameter(name));
        if (result.isBlank()) throw new IllegalArgumentException(message);
        return result;
    }

    private long positiveLong(HttpServletRequest request, String name, String message) {
        try {
            long result = Long.parseLong(value(request.getParameter(name)));
            if (result > 0) return result;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException(message);
    }

    private int positiveInt(HttpServletRequest request, String name, String message) {
        try {
            int result = Integer.parseInt(value(request.getParameter(name)));
            if (result > 0) return result;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException(message);
    }

    private LocalDate date(HttpServletRequest request, String name, String message) {
        try {
            return LocalDate.parse(value(request.getParameter(name)));
        } catch (Exception ignored) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
        }
    }

    private String normaliseStatus(String status) {
        return switch (value(status).toUpperCase()) {
            case "ACTIVE", "COMPLETED", "CANCELLED" -> value(status).toUpperCase();
            default -> "PLANNING";
        };
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }
}
