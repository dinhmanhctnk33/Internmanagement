package com.example.internmanagement.controller;

import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/mentor-assignments")
public class MentorAssignmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isHrOrAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Long programId = parseId(request.getParameter("programId"));
        String assignment = normaliseAssignment(request.getParameter("assignment"));
        String keyword = clean(request.getParameter("q"));

        try (Connection connection = DBConnection.getConnection()) {
            List<Map<String, Object>> interns = loadInterns(connection, programId, assignment, keyword);
            List<Map<String, Object>> mentors = loadMentors(connection);
            request.setAttribute("interns", interns);
            request.setAttribute("mentors", mentors);
            request.setAttribute("programs", loadPrograms(connection));
            request.setAttribute("metrics", calculateMetrics(connection));
            request.setAttribute("selectedProgramId", programId);
            request.setAttribute("selectedAssignment", assignment);
            request.setAttribute("keyword", keyword);
            request.setAttribute("currentUser", request.getSession().getAttribute("user"));
            request.getRequestDispatcher("/WEB-INF/views/mentor-assignments.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải dữ liệu phân công mentor.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isHrOrAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        String action = clean(request.getParameter("action"));
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long profileId = requiredId(request.getParameter("profileId"), "Thực tập sinh không hợp lệ.");
                if ("unassign".equals(action)) {
                    unassign(connection, profileId);
                } else {
                    long mentorId = requiredId(request.getParameter("mentorId"), "Vui lòng chọn mentor.");
                    assign(connection, profileId, mentorId);
                }
                connection.commit();
                redirect(response, request, "success=" + ("unassign".equals(action) ? "unassigned" : "assigned"));
            } catch (IllegalArgumentException | SQLException exception) {
                connection.rollback();
                String message = exception instanceof IllegalArgumentException
                        ? exception.getMessage() : "Không thể lưu phân công. Vui lòng thử lại.";
                redirect(response, request, "error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            redirect(response, request, "error=" + URLEncoder.encode(
                    "Không thể kết nối cơ sở dữ liệu.", StandardCharsets.UTF_8));
        }
    }

    private void assign(Connection connection, long profileId, long mentorId) throws SQLException {
        Map<String, Object> intern = lockIntern(connection, profileId);
        Map<String, Object> mentor = lockMentor(connection, mentorId);
        Long programDepartment = asLong(intern.get("department_id"));
        Long mentorDepartment = asLong(mentor.get("mentor_department_id"));
        if (programDepartment != null && mentorDepartment != null && !programDepartment.equals(mentorDepartment)) {
            throw new IllegalArgumentException("Mentor không thuộc phòng ban của chương trình thực tập.");
        }

        long currentMentorId = asLong(intern.get("mentor_id")) == null ? -1 : asLong(intern.get("mentor_id"));
        if (currentMentorId != mentorId) {
            int currentLoad = mentorLoad(connection, mentorId, profileId);
            int maxCapacity = ((Number) mentor.get("max_capacity")).intValue();
            if (currentLoad >= maxCapacity) {
                throw new IllegalArgumentException("Mentor đã đạt số lượng thực tập sinh tối đa.");
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE intern_profiles SET mentor_id=? WHERE id=? AND internship_status='ACTIVE'")) {
            statement.setLong(1, mentorId);
            statement.setLong(2, profileId);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy thực tập sinh đang hoạt động.");
            }
        }
    }

    private void unassign(Connection connection, long profileId) throws SQLException {
        lockIntern(connection, profileId);
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE intern_profiles SET mentor_id=NULL WHERE id=? AND internship_status='ACTIVE'")) {
            statement.setLong(1, profileId);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy thực tập sinh đang hoạt động.");
            }
        }
    }

    private Map<String, Object> lockIntern(Connection connection, long profileId) throws SQLException {
        String sql = "SELECT ip.id,ip.mentor_id,p.department_id FROM intern_profiles ip "
                + "LEFT JOIN internship_programs p ON p.id=ip.program_id "
                + "WHERE ip.id=? AND ip.internship_status='ACTIVE' FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, profileId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Không tìm thấy thực tập sinh đang hoạt động.");
                return row(result);
            }
        }
    }

    private Map<String, Object> lockMentor(Connection connection, long mentorId) throws SQLException {
        String sql = "SELECT m.id,m.department_id mentor_department_id,m.max_capacity,m.status,u.full_name "
                + "FROM mentors m JOIN users u ON u.id=m.user_id WHERE m.id=? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mentorId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Không tìm thấy mentor.");
                Map<String, Object> mentor = row(result);
                if (!"ACTIVE".equalsIgnoreCase(String.valueOf(mentor.get("status")))) {
                    throw new IllegalArgumentException("Mentor này hiện không hoạt động.");
                }
                return mentor;
            }
        }
    }

    private int mentorLoad(Connection connection, long mentorId, long excludedProfileId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM intern_profiles WHERE mentor_id=? AND internship_status='ACTIVE' AND id<>?")) {
            statement.setLong(1, mentorId);
            statement.setLong(2, excludedProfileId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private List<Map<String, Object>> loadInterns(Connection connection, Long programId,
                                                   String assignment, String keyword) throws SQLException {
        String sql = "SELECT ip.id,ip.intern_code,ip.program_id,ip.mentor_id,u.full_name,u.email," 
                + "p.program_name,p.department_id,d.dept_name,mu.full_name mentor_name,m.mentor_code "
                + "FROM intern_profiles ip JOIN users u ON u.id=ip.user_id "
                + "LEFT JOIN internship_programs p ON p.id=ip.program_id LEFT JOIN departments d ON d.id=p.department_id "
                + "LEFT JOIN mentors m ON m.id=ip.mentor_id LEFT JOIN users mu ON mu.id=m.user_id "
                + "WHERE ip.internship_status='ACTIVE' AND (? IS NULL OR ip.program_id=?) "
                + "AND (?='ALL' OR (?='ASSIGNED' AND ip.mentor_id IS NOT NULL) OR (?='UNASSIGNED' AND ip.mentor_id IS NULL)) "
                + "AND (?='' OR u.full_name LIKE CONCAT('%',?,'%') OR ip.intern_code LIKE CONCAT('%',?,'%')) "
                + "ORDER BY (ip.mentor_id IS NULL) DESC,u.full_name";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setNullableLong(statement, 1, programId);
            setNullableLong(statement, 2, programId);
            statement.setString(3, assignment);
            statement.setString(4, assignment);
            statement.setString(5, assignment);
            statement.setString(6, keyword);
            statement.setString(7, keyword);
            statement.setString(8, keyword);
            return rows(statement);
        }
    }

    private List<Map<String, Object>> loadMentors(Connection connection) throws SQLException {
        String sql = "SELECT m.id,m.mentor_code,m.department_id,m.max_capacity,m.status,u.full_name,u.email,d.dept_name," 
                + "COUNT(CASE WHEN ip.internship_status='ACTIVE' THEN 1 END) load_count "
                + "FROM mentors m JOIN users u ON u.id=m.user_id LEFT JOIN departments d ON d.id=m.department_id "
                + "LEFT JOIN intern_profiles ip ON ip.mentor_id=m.id WHERE m.status='ACTIVE' "
                + "GROUP BY m.id,m.mentor_code,m.department_id,m.max_capacity,m.status,u.full_name,u.email,d.dept_name "
                + "ORDER BY d.dept_name,u.full_name";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return rows(statement);
        }
    }

    private List<Map<String, Object>> loadPrograms(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id,program_code,program_name FROM internship_programs ORDER BY program_name")) {
            return rows(statement);
        }
    }

    private Map<String, Object> calculateMetrics(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) total_interns,COUNT(ip.mentor_id) assigned_interns," 
                + "SUM(ip.mentor_id IS NULL) unassigned_interns FROM intern_profiles ip WHERE ip.internship_status='ACTIVE'";
        Map<String, Object> metrics;
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            result.next();
            metrics = row(result);
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) active_mentors,COALESCE(SUM(max_capacity),0) total_capacity FROM mentors WHERE status='ACTIVE'");
             ResultSet result = statement.executeQuery()) {
            result.next();
            metrics.put("active_mentors", result.getInt("active_mentors"));
            metrics.put("total_capacity", result.getInt("total_capacity"));
        }
        return metrics;
    }

    private List<Map<String, Object>> rows(PreparedStatement statement) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.add(row(rs));
        }
        return result;
    }

    private Map<String, Object> row(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int index = 1; index <= rs.getMetaData().getColumnCount(); index++) {
            row.put(rs.getMetaData().getColumnLabel(index), rs.getObject(index));
        }
        return row;
    }

    private void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) statement.setNull(index, Types.BIGINT);
        else statement.setLong(index, value);
    }

    private Long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private long requiredId(String value, String message) {
        Long id = parseId(value);
        if (id == null) throw new IllegalArgumentException(message);
        return id;
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

    private String normaliseAssignment(String value) {
        return "ASSIGNED".equals(value) || "UNASSIGNED".equals(value) ? value : "ALL";
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private void redirect(HttpServletResponse response, HttpServletRequest request, String query) throws IOException {
        response.sendRedirect(request.getContextPath() + "/mentor-assignments?" + query);
    }

    private boolean isHrOrAdmin(HttpServletRequest request) {
        String role = String.valueOf(request.getSession().getAttribute("userRole"));
        return "HR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }
}
