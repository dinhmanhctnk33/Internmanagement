package com.example.internmanagement.controller;

import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** SCRUM-43 / SCRUM-153: HR thêm mới và quản lý hồ sơ mentor. */
@WebServlet("/mentors")
public class MentorServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$", Pattern.CASE_INSENSITIVE);
    private static final String TOKEN_ATTR = "mentorFormTokens";
    // "Đang hướng dẫn" = TTS đang ACTIVE, được gán cho mentor và kỳ thực tập chưa kết thúc
    private static final String INTERN_COUNT_SQL = "SELECT COUNT(*) FROM intern_profiles WHERE mentor_id=? "
            + "AND internship_status='ACTIVE' AND (end_date IS NULL OR end_date>=CURDATE())";

    /** Báo lỗi xung đột phiên bản: không render lại form mà bắt HR tải lại trang. */
    private static class ConflictException extends IllegalArgumentException {
        ConflictException(String message) { super(message); }
    }

    // ------------------------------------------------------------------ GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isHr(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền sử dụng chức năng này.");
            return;
        }
        render(request, response, null);
    }

    private void render(HttpServletRequest request, HttpServletResponse response, Map<String, Object> form)
            throws ServletException, IOException {
        request.setAttribute("currentUser", request.getSession().getAttribute("user"));
        String deptFilter = filter(request, "departmentId");
        String activeFilter = oneOf(filter(request, "active"), "ACTIVE", "INACTIVE");
        String linkFilter = oneOf(filter(request, "link"), "LINKED", "UNLINKED");
        String keyword = filter(request, "q");
        request.setAttribute("selectedDepartmentId", parseId(deptFilter));
        request.setAttribute("selectedActive", activeFilter);
        request.setAttribute("selectedLink", linkFilter);
        request.setAttribute("keyword", keyword);
        request.setAttribute("formToken", newToken(request));
        request.setAttribute("form", form);
        try (Connection connection = DBConnection.getConnection()) {
            request.setAttribute("mentors", loadMentors(connection, parseId(deptFilter), activeFilter, linkFilter, keyword));
            request.setAttribute("departments", rows(connection,
                    "SELECT id,dept_name FROM departments WHERE is_active=TRUE ORDER BY dept_name"));
            request.setAttribute("unlinkedInternCount", scalar(connection,
                    "SELECT COUNT(*) FROM intern_profiles ip JOIN mentors m ON m.id=ip.mentor_id "
                            + "WHERE m.user_id IS NULL AND ip.internship_status='ACTIVE' "
                            + "AND (ip.end_date IS NULL OR ip.end_date>=CURDATE())"));
        } catch (SQLException exception) {
            // Không hiện "danh sách trống": JSP hiện lỗi + nút thử lại
            request.setAttribute("loadError", "Không thể tải danh sách mentor. Vui lòng thử lại.");
        }
        request.getRequestDispatcher("/WEB-INF/views/mentors.jsp").forward(request, response);
    }

    /** Sau POST lỗi, bộ lọc nằm trong f_* (tên "departmentId" lúc đó là ô phòng ban của form). */
    private String filter(HttpServletRequest request, String name) {
        return clean(request.getParameter("POST".equals(request.getMethod()) ? "f_" + name : name));
    }

    private List<Map<String, Object>> loadMentors(Connection connection, Long departmentId, String active,
                                                  String link, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT m.id,m.mentor_code,m.department_id,d.dept_name,m.status,m.version,m.user_id,m.full_name,"
                + "COALESCE(u.email,m.email) email,u.status account_status,"
                + "(SELECT COUNT(*) FROM intern_profiles ip WHERE ip.mentor_id=m.id AND ip.internship_status='ACTIVE' "
                + "   AND (ip.end_date IS NULL OR ip.end_date>=CURDATE())) intern_count,"
                + "(SELECT COUNT(*) FROM intern_profiles ip WHERE ip.mentor_id=m.id) assigned_total "
                + "FROM mentors m LEFT JOIN users u ON u.id=m.user_id LEFT JOIN departments d ON d.id=m.department_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (departmentId != null) { sql.append(" AND m.department_id=?"); params.add(departmentId); }
        if (!active.isEmpty()) { sql.append(" AND m.status=?"); params.add(active); }
        if ("LINKED".equals(link)) sql.append(" AND m.user_id IS NOT NULL");
        if ("UNLINKED".equals(link)) sql.append(" AND m.user_id IS NULL");
        if (!keyword.isEmpty()) {
            sql.append(" AND (m.full_name LIKE CONCAT('%',?,'%') OR COALESCE(u.email,m.email) LIKE CONCAT('%',?,'%'))");
            params.add(keyword); params.add(keyword);
        }
        sql.append(" ORDER BY d.dept_name,m.full_name");
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) statement.setObject(i + 1, params.get(i));
            return rows(statement);
        }
    }
    
    // ----------------------------------------------------------------- POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isHr(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền sử dụng chức năng này.");
            return;
        }
        request.setCharacterEncoding("UTF-8");
        String action = clean(request.getParameter("action"));
        boolean formAction = "create".equals(action) || "update".equals(action);
        // Bấm lưu 2 lần: token đã bị dùng -> không tạo thêm hồ sơ
        if (formAction && !consumeToken(request, request.getParameter("token"))) {
            redirect(response, request, "info=duplicate");
            return;
        }
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String result = switch (action) {
                    case "create" -> create(connection, request);
                    case "update" -> update(connection, request);
                    case "toggle" -> toggle(connection, request);
                    case "delete" -> delete(connection, request);
                    default -> throw new IllegalArgumentException("Thao tác không hợp lệ.");
                };
                connection.commit();
                redirect(response, request, "success=" + result);
            } catch (IllegalArgumentException exception) {
                connection.rollback();
                fail(request, response, action, exception.getMessage(), !(exception instanceof ConflictException));
            } catch (SQLException exception) {
                connection.rollback();
                String message = exception.getSQLState() != null && exception.getSQLState().startsWith("23")
                        ? "Email này đã có hồ sơ mentor khác." : "Không thể lưu dữ liệu. Vui lòng thử lại.";
                fail(request, response, action, message, true);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            redirect(response, request, "error=" + enc("Không thể kết nối cơ sở dữ liệu."));
        }
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String action, String message,
                      boolean keepForm) throws ServletException, IOException {
        if (keepForm && ("create".equals(action) || "update".equals(action))) {
            // Giữ lại dữ liệu HR đã nhập, mở lại hộp thoại kèm lỗi
            Map<String, Object> form = new LinkedHashMap<>();
            form.put("mode", action);
            form.put("id", request.getParameter("id"));
            form.put("version", request.getParameter("version"));
            form.put("fullName", clean(request.getParameter("fullName")));
            form.put("email", clean(request.getParameter("email")));
            form.put("departmentId", parseId(request.getParameter("departmentId")));
            form.put("linked", "1".equals(request.getParameter("linked")));
            form.put("internCount", request.getParameter("internCount"));
            form.put("error", message);
            render(request, response, form);
        } else {
            redirect(response, request, "error=" + enc(message));
        }
    }
    
    // --------------------------------------------------------------- create
    private String create(Connection connection, HttpServletRequest request) throws SQLException {
        String name = cleanName(request.getParameter("fullName"));
        String email = normaliseEmail(request.getParameter("email"));
        Long departmentId = parseId(request.getParameter("departmentId"));
        validate(connection, name, email, departmentId);
        assertEmailFree(connection, email, null);
        Long accountId = findMentorAccount(connection, email); // null = chưa có tài khoản

        long id;
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO mentors(user_id,full_name,email,mentor_code,department_id,status) VALUES(?,?,?,?,?,'ACTIVE')",
                Statement.RETURN_GENERATED_KEYS)) {
            setNullableLong(statement, 1, accountId);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, "TMP" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
            statement.setLong(5, departmentId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không nhận được ID hồ sơ mentor.");
                id = keys.getLong(1);
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("UPDATE mentors SET mentor_code=? WHERE id=?")) {
            statement.setString(1, String.format("MT%04d", id));
            statement.setLong(2, id);
            statement.executeUpdate();
        }
        audit(connection, actorId(request), "CREATE_MENTOR", id, null,
                json("full_name", name, "email", email, "department_id", departmentId,
                        "status", "ACTIVE", "user_id", accountId), request.getRemoteAddr());
        return "created";
    }

    // --------------------------------------------------------------- update
    private String update(Connection connection, HttpServletRequest request) throws SQLException {
        long id = requiredId(request.getParameter("id"));
        Map<String, Object> current = lockMentor(connection, id);
        if (asInt(current.get("version")) != parseInt(request.getParameter("version"))) {
            throw new ConflictException("Hồ sơ đã được cập nhật bởi người khác. Vui lòng tải lại trang trước khi lưu.");
        }
        String name = cleanName(request.getParameter("fullName"));
        String email = normaliseEmail(request.getParameter("email"));
        Long departmentId = parseId(request.getParameter("departmentId"));
        validate(connection, name, email, departmentId);

        Long accountId = asLong(current.get("user_id"));
        String currentEmail = String.valueOf(accountId != null ? current.get("account_email") : current.get("email"));
        String storedEmail = String.valueOf(current.get("email"));
        if (!email.equalsIgnoreCase(currentEmail)) {
            if (accountId != null) {
                throw new IllegalArgumentException("Mentor đã liên kết tài khoản nên email là khóa liên kết. "
                        + "Vui lòng liên hệ quản trị viên để đổi email.");
            }
            assertEmailFree(connection, email, id);
            accountId = findMentorAccount(connection, email); // email mới đã có tài khoản mentor -> liên kết luôn
            storedEmail = email;
        }

        Long oldDepartment = asLong(current.get("department_id"));
        int internCount = internCount(connection, id);
        if (!Objects.equals(oldDepartment, departmentId) && internCount > 0
                && !"1".equals(request.getParameter("confirmDepartment"))) {
            throw new IllegalArgumentException("Mentor đang hướng dẫn " + internCount + " thực tập sinh. Phân công cũ "
                    + "giữ nguyên, phân công mới phải cùng phòng ban. Vui lòng xác nhận để đổi phòng ban.");
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE mentors SET full_name=?,email=?,department_id=?,user_id=?,version=version+1 WHERE id=? AND version=?")) {
            statement.setString(1, name);
            statement.setString(2, storedEmail);
            statement.setLong(3, departmentId);
            setNullableLong(statement, 4, accountId);
            statement.setLong(5, id);
            statement.setInt(6, asInt(current.get("version")));
            if (statement.executeUpdate() == 0) {
                throw new ConflictException("Hồ sơ đã được cập nhật bởi người khác. Vui lòng tải lại trang trước khi lưu.");
            }
        }
        audit(connection, actorId(request), "UPDATE_MENTOR", id,
                json("full_name", current.get("full_name"), "email", currentEmail, "department_id", oldDepartment),
                json("full_name", name, "email", email, "department_id", departmentId), request.getRemoteAddr());
        return "updated";
    }

    // --------------------------------------------------------------- toggle
    private String toggle(Connection connection, HttpServletRequest request) throws SQLException {
        long id = requiredId(request.getParameter("id"));
        String target = "ACTIVE".equals(request.getParameter("status")) ? "ACTIVE" : "INACTIVE";
        Map<String, Object> current = lockMentor(connection, id);
        String oldStatus = String.valueOf(current.get("status"));
        if (oldStatus.equals(target)) return "updated";
        if ("INACTIVE".equals(target)) {
            int internCount = internCount(connection, id);
            if (internCount > 0 && !"1".equals(request.getParameter("confirmDeactivate"))) {
                throw new IllegalArgumentException("Mentor đang hướng dẫn " + internCount
                        + " thực tập sinh. Họ vẫn giữ mentor này, hãy đổi mentor nếu cần.");
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE mentors SET status=?,version=version+1 WHERE id=?")) {
            statement.setString(1, target);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
        audit(connection, actorId(request), "ACTIVE".equals(target) ? "ACTIVATE_MENTOR" : "DEACTIVATE_MENTOR", id,
                json("status", oldStatus), json("status", target), request.getRemoteAddr());
        return "ACTIVE".equals(target) ? "activated" : "deactivated";
    }

    // --------------------------------------------------------------- delete
    private String delete(Connection connection, HttpServletRequest request) throws SQLException {
        long id = requiredId(request.getParameter("id"));
        Map<String, Object> current = lockMentor(connection, id);
        long used = scalarWithId(connection, "SELECT COUNT(*) FROM intern_profiles WHERE mentor_id=?", id)
                + scalarWithId(connection, "SELECT COUNT(*) FROM tasks WHERE mentor_id=?", id)
                + scalarWithId(connection, "SELECT COUNT(*) FROM intern_evaluations WHERE mentor_id=?", id);
        if (used > 0) {
            throw new IllegalArgumentException("Hồ sơ mentor đã có phân công hoặc dữ liệu liên quan nên không thể xóa. "
                    + "Hãy chuyển sang ngừng hoạt động.");
        }
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM mentors WHERE id=?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
        audit(connection, actorId(request), "DELETE_MENTOR", id,
                json("full_name", current.get("full_name"), "email", current.get("email")), null, request.getRemoteAddr());
        return "deleted";
    }
    
    // -------------------------------------------------------------- helpers
    private void validate(Connection connection, String name, String email, Long departmentId) throws SQLException {
        if (name.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập họ tên mentor.");
        if (name.length() > 100) throw new IllegalArgumentException("Họ tên tối đa 100 ký tự.");
        if (email.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập email.");
        if (email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
        if (departmentId == null) throw new IllegalArgumentException("Vui lòng chọn phòng ban.");
        if (scalarWithId(connection, "SELECT COUNT(*) FROM departments WHERE is_active=TRUE AND id=?", departmentId) == 0) {
            throw new IllegalArgumentException("Phòng ban không hợp lệ.");
        }
    }

    /** Email đã có hồ sơ mentor khác (không phân biệt hoa thường) thì chặn và chỉ ra hồ sơ trùng. */
    private void assertEmailFree(Connection connection, String email, Long excludedId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT m.full_name FROM mentors m LEFT JOIN users u ON u.id=m.user_id "
                        + "WHERE (LOWER(m.email)=LOWER(?) OR LOWER(u.email)=LOWER(?)) AND (? IS NULL OR m.id<>?) LIMIT 1")) {
            statement.setString(1, email);
            statement.setString(2, email);
            setNullableLong(statement, 3, excludedId);
            setNullableLong(statement, 4, excludedId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    throw new IllegalArgumentException("Email này đã có hồ sơ mentor: " + result.getString(1) + ".");
                }
            }
        }
    }

    /**
     * Trả về id tài khoản MENTOR trùng email (để liên kết ngay), null nếu chưa có tài khoản.
     * Nếu email thuộc tài khoản không phải mentor thì chặn, không tự gắn.
     */
    private Long findMentorAccount(Connection connection, String email) throws SQLException {
        String sql = "SELECT u.id,"
                + "EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.role_code='MENTOR') is_mentor,"
                + "EXISTS(SELECT 1 FROM mentors m WHERE m.user_id=u.id) has_profile "
                + "FROM users u WHERE LOWER(u.email)=LOWER(?) LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                if (!result.getBoolean("is_mentor")) {
                    throw new IllegalArgumentException("Email này đã dùng cho tài khoản khác.");
                }
                if (result.getBoolean("has_profile")) {
                    throw new IllegalArgumentException("Tài khoản mentor này đã gắn với một hồ sơ khác.");
                }
                return result.getLong("id");
            }
        }
    }

    private Map<String, Object> lockMentor(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT m.id,m.user_id,m.full_name,m.email,m.department_id,m.status,m.version,u.email account_email "
                        + "FROM mentors m LEFT JOIN users u ON u.id=m.user_id WHERE m.id=? FOR UPDATE")) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Không tìm thấy hồ sơ mentor.");
                return row(result);
            }
        }
    }

    private int internCount(Connection connection, long mentorId) throws SQLException {
        return (int) scalarWithId(connection, INTERN_COUNT_SQL, mentorId);
    }

    private void audit(Connection connection, Long actorId, String action, long recordId, String oldJson,
                       String newJson, String ip) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO audit_logs(user_id,action_code,table_name,record_id,old_values_json,new_values_json,ip_address) "
                        + "VALUES(?,?,'mentors',?,?,?,?)")) {
            setNullableLong(statement, 1, actorId);
            statement.setString(2, action);
            statement.setLong(3, recordId);
            statement.setString(4, oldJson);
            statement.setString(5, newJson);
            statement.setString(6, ip);
            statement.executeUpdate();
        }
    }

    private static String json(Object... pairs) {
        StringBuilder out = new StringBuilder("{");
        for (int i = 0; i < pairs.length; i += 2) {
            if (i > 0) out.append(',');
            out.append('"').append(pairs[i]).append("\":");
            Object value = pairs[i + 1];
            if (value == null) out.append("null");
            else if (value instanceof Number) out.append(value);
            else out.append('"').append(String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        return out.append('}').toString();
    }

    @SuppressWarnings("unchecked")
    private String newToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Set<String> tokens = (Set<String>) session.getAttribute(TOKEN_ATTR);
        if (tokens == null) {
            tokens = ConcurrentHashMap.newKeySet();
            session.setAttribute(TOKEN_ATTR, tokens);
        }
        if (tokens.size() > 20) tokens.clear();
        String token = UUID.randomUUID().toString();
        tokens.add(token);
        return token;
    }

    @SuppressWarnings("unchecked")
    private boolean consumeToken(HttpServletRequest request, String token) {
        Set<String> tokens = (Set<String>) request.getSession().getAttribute(TOKEN_ATTR);
        return tokens != null && token != null && tokens.remove(token);
    }

    private List<Map<String, Object>> rows(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return rows(statement);
        }
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
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
        }
        return row;
    }

    private long scalar(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private long scalarWithId(Connection connection, String sql, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.BIGINT);
        else statement.setLong(index, value);
    }

    private Long asLong(Object value) { return value instanceof Number n ? n.longValue() : null; }
    private int asInt(Object value) { return value instanceof Number n ? n.intValue() : 0; }

    private int parseInt(String value) {
        try { return Integer.parseInt(clean(value)); } catch (NumberFormatException e) { return -1; }
    }

    private long requiredId(String value) {
        Long id = parseId(value);
        if (id == null) throw new IllegalArgumentException("Hồ sơ mentor không hợp lệ.");
        return id;
    }

    private Long parseId(String value) {
        try {
            long id = Long.parseLong(clean(value));
            return id > 0 ? id : null;
        } catch (NumberFormatException e) { return null; }
    }

    private String oneOf(String value, String... allowed) {
        for (String option : allowed) if (option.equals(value)) return option;
        return "";
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
    private String cleanName(String value) { return clean(value).replaceAll("\\s+", " "); }
    private String normaliseEmail(String value) { return clean(value).toLowerCase(java.util.Locale.ROOT); }
    private String enc(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private void redirect(HttpServletResponse response, HttpServletRequest request, String query) throws IOException {
        StringBuilder target = new StringBuilder(request.getContextPath()).append("/mentors?").append(query);
        for (String name : new String[]{"departmentId", "active", "link", "q"}) {
            String value = request.getParameter("f_" + name); // bộ lọc đang chọn, gửi kèm từ form
            if (value != null && !value.isBlank()) target.append('&').append(name).append('=').append(enc(value));
        }
        response.sendRedirect(target.toString());
    }

    private Long actorId(HttpServletRequest request) {
        Object user = request.getSession().getAttribute("user");
        if (user instanceof User u) return (long) u.getId();
        throw new IllegalArgumentException("Phiên đăng nhập không hợp lệ.");
    }

    private boolean isHr(HttpServletRequest request) {
        return "HR".equalsIgnoreCase(String.valueOf(request.getSession().getAttribute("userRole")));
    }
}