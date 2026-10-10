package com.example.internmanagement.controller;

import com.example.internmanagement.util.DBConnection;
import com.example.internmanagement.util.EmailUtility;
import com.example.internmanagement.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet({"/admin/users", "/admin/accounts", "/admin/permissions"})
public class AdminServlet extends HttpServlet {
    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$", java.util.regex.Pattern.CASE_INSENSITIVE);
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection connection = DBConnection.getConnection()) {
            ensurePermissionStorage(connection);
            loadPage(request, connection);
            String view = request.getServletPath().endsWith("permissions")
                    ? "/WEB-INF/views/admin-permissions.jsp"
                    : request.getServletPath().endsWith("accounts")
                    ? "/WEB-INF/views/admin-accounts.jsp"
                    : "/WEB-INF/views/dashboard-admin.jsp";
            request.getRequestDispatcher(view).forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Không thể tải cấu hình quản trị. Hãy chạy migration-001-rbac.sql nếu đây là database cũ.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (request.getServletPath().endsWith("users") || request.getServletPath().endsWith("accounts")) {
            String action = value(request, "action");
            if ("change-status".equals(action)) {
                changeUserStatus(request, response);
            } else if ("reset-password".equals(action)) {
                resetUserPassword(request, response);
            } else {
                createUser(request, response);
            }
        } else {
            savePermissions(request, response);
        }
    }

    private void savePermissions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String roleCode = value(request, "role");
        String[] requestedPermissions = request.getParameterValues("permission");

        try (Connection connection = DBConnection.getConnection()) {
            ensurePermissionStorage(connection);
            connection.setAutoCommit(false);
            try {
                int roleId = findRoleId(connection, roleCode);
                Set<String> availablePermissions = permissionCodes(connection);
                Set<String> selectedPermissions = new HashSet<>();
                if (requestedPermissions != null) {
                    for (String permission : requestedPermissions) {
                        if (!availablePermissions.contains(permission)) {
                            throw new IllegalArgumentException("Quyền không hợp lệ: " + permission);
                        }
                        selectedPermissions.add(permission);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM role_permissions WHERE role_id = ?")) {
                    statement.setInt(1, roleId);
                    statement.executeUpdate();
                }
                if (!selectedPermissions.isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO role_permissions(role_id, permission_id) "
                                    + "SELECT ?, id FROM permissions WHERE permission_code = ?")) {
                        for (String permission : selectedPermissions) {
                            statement.setInt(1, roleId);
                            statement.setString(2, permission);
                            statement.addBatch();
                        }
                        statement.executeBatch();
                    }
                }
                connection.commit();
                response.sendRedirect(request.getContextPath() + "/admin/permissions?updated=1&role=" + roleCode);
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/admin/permissions?error=" + urlEncode(e.getMessage()));
        } catch (SQLException e) {
            throw new ServletException("Không thể lưu phân quyền. Hãy chạy migration-001-rbac.sql nếu bảng role_permissions chưa tồn tại.", e);
        }
    }

    private void createUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = value(request, "username");
        String password = value(request, "password");
        String confirmPassword = value(request, "confirmPassword");
        String email = value(request, "email").toLowerCase(java.util.Locale.ROOT);
        String fullName = value(request, "fullName");
        String roleCode = value(request, "role");
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error="
                    + urlEncode("Vui lòng nhập đầy đủ các trường bắt buộc."));
            return;
        }
        if (email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error="
                    + urlEncode("Email không đúng định dạng."));
            return;
        }
        if (password.length() < 6) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error="
                    + urlEncode("Mật khẩu phải có ít nhất 6 ký tự."));
            return;
        }
        if (!password.equals(confirmPassword)) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error="
                    + urlEncode("Mật khẩu nhập lại không khớp."));
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (emailExists(connection, email)) {
                    throw new IllegalArgumentException("Email này đã được sử dụng cho một tài khoản khác.");
                }
                int roleId = findRoleId(connection, roleCode);
                int userId;
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO users(username,password,email,full_name,status,create_at,update_at,c_password) "
                                + "VALUES(?,?,?,?, 'ACTIVE',CURDATE(),CURDATE(),false)", Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, username);
                    statement.setString(2, PasswordUtil.hash(password));
                    statement.setString(3, email);
                    statement.setString(4, fullName);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không nhận được ID tài khoản mới.");
                        userId = keys.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO user_roles(user_id,role_id) VALUES(?,?)")) {
                    statement.setInt(1, userId);
                    statement.setInt(2, roleId);
                    statement.executeUpdate();
                }
                connection.commit();
                String notification = "not-required";
                if ("HR".equalsIgnoreCase(roleCode) || "INTERN".equalsIgnoreCase(roleCode)) {
                    try {
                        EmailUtility.sendAccountCreatedEmail(email, fullName, username, password,
                                roleDisplayName(roleCode), loginUrl(request));
                        notification = "sent";
                    } catch (Exception ignored) {
                        notification = "failed";
                    }
                }
                response.sendRedirect(request.getContextPath() + "/admin/users?created=1&notification=" + notification);
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error=" + urlEncode(e.getMessage()));
        } catch (SQLException e) {
            throw new ServletException("Không thể tạo tài khoản quản trị.", e);
        }
    }

    private void changeUserStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try (Connection connection = DBConnection.getConnection()) {
            int userId = parseUserId(request);
            preventSelfManagement(request, userId);
            String newStatus = "ACTIVE".equals(value(request, "status")) ? "ACTIVE" : "INACTIVE";
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET status = ?, update_at = CURDATE() WHERE id = ?")) {
                statement.setString(1, newStatus);
                statement.setInt(2, userId);
                if (statement.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Không tìm thấy tài khoản cần cập nhật.");
                }
            }
            response.sendRedirect(accountManagementUrl(request) + "?result=status-updated");
        } catch (IllegalArgumentException e) {
            response.sendRedirect(accountManagementUrl(request) + "?error=" + urlEncode(e.getMessage()));
        } catch (SQLException e) {
            throw new ServletException("Không thể cập nhật trạng thái tài khoản.", e);
        }
    }

    private void resetUserPassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String newPassword = value(request, "newPassword");
        if (newPassword.length() < 6) {
            response.sendRedirect(accountManagementUrl(request) + "?error="
                    + urlEncode("Mật khẩu mới phải có ít nhất 6 ký tự."));
            return;
        }
        try (Connection connection = DBConnection.getConnection()) {
            int userId = parseUserId(request);
            preventSelfManagement(request, userId);
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET password = ?, c_password = false, act_token = NULL, ex_date_at = NULL, update_at = CURDATE() WHERE id = ?")) {
                statement.setString(1, PasswordUtil.hash(newPassword));
                statement.setInt(2, userId);
                if (statement.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Không tìm thấy tài khoản cần đặt lại mật khẩu.");
                }
            }
            response.sendRedirect(accountManagementUrl(request) + "?result=password-reset");
        } catch (IllegalArgumentException e) {
            response.sendRedirect(accountManagementUrl(request) + "?error=" + urlEncode(e.getMessage()));
        } catch (SQLException e) {
            throw new ServletException("Không thể đặt lại mật khẩu tài khoản.", e);
        }
    }

    private int parseUserId(HttpServletRequest request) {
        try {
            int id = Integer.parseInt(value(request, "userId"));
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tài khoản không hợp lệ.");
        }
    }

    private void preventSelfManagement(HttpServletRequest request, int targetUserId) {
        com.example.internmanagement.model.User currentUser =
                (com.example.internmanagement.model.User) request.getSession().getAttribute("user");
        if (currentUser != null && currentUser.getId() == targetUserId) {
            throw new IllegalArgumentException("Bạn không thể khóa hoặc đặt lại mật khẩu của chính mình tại đây.");
        }
    }

    private String accountManagementUrl(HttpServletRequest request) {
        return request.getContextPath() + (request.getServletPath().endsWith("accounts")
                ? "/admin/accounts" : "/admin/users");
    }

    private String loginUrl(HttpServletRequest request) {
        int port = request.getServerPort();
        String portPart = (port == 80 || port == 443) ? "" : ":" + port;
        return request.getScheme() + "://" + request.getServerName() + portPart
                + request.getContextPath() + "/login";
    }

    private String roleDisplayName(String roleCode) {
        if ("HR".equalsIgnoreCase(roleCode)) return "Phòng Nhân sự (HR)";
        if ("INTERN".equalsIgnoreCase(roleCode)) return "Thực tập sinh";
        return roleCode;
    }

    private void loadPage(HttpServletRequest request, Connection connection) throws SQLException {
        String selectedRole = value(request, "role");
        List<String> roles = roleCodes(connection);
        if (!roles.contains(selectedRole)) selectedRole = roles.contains("HR") ? "HR" : roles.get(0);

        request.setAttribute("users", users(connection));
        request.setAttribute("roles", roles);
        request.setAttribute("permissions", permissionCodes(connection));
        request.setAttribute("selectedRole", selectedRole);
        request.setAttribute("selectedPermissions", selectedPermissions(connection, selectedRole));
    }

    /** Keeps deployments made before RBAC was introduced compatible with this screen. */
    private void ensurePermissionStorage(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS role_permissions ("
                    + "role_id BIGINT NOT NULL, permission_id BIGINT NOT NULL, "
                    + "PRIMARY KEY (role_id, permission_id), "
                    + "CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE ON UPDATE CASCADE) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate("INSERT IGNORE INTO permissions(permission_code,permission_name,module_group) VALUES "
                    + "('INTERN_CREATE','Tạo hồ sơ thực tập sinh','INTERN'),"
                    + "('INTERN_EDIT','Chỉnh sửa hồ sơ thực tập sinh','INTERN'),"
                    + "('DOCUMENT_REVIEW','Duyệt tài liệu thực tập sinh','DOCUMENT'),"
                    + "('DOCUMENT_UPLOAD','Tải tài liệu thực tập sinh','DOCUMENT')");
        }
    }

    private List<Map<String, String>> users(Connection connection) throws SQLException {
        String sql = "SELECT u.id,u.username,u.full_name,u.email,u.status,COALESCE(r.role_code,'UNASSIGNED') role_code "
                + "FROM users u LEFT JOIN user_roles ur ON ur.user_id=u.id "
                + "LEFT JOIN roles r ON r.id=ur.role_id ORDER BY u.id DESC";
        List<Map<String, String>> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Map<String, String> row = new HashMap<>();
                for (String column : List.of("id", "username", "full_name", "email", "status", "role_code")) {
                    row.put(column, results.getString(column));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private List<String> roleCodes(Connection connection) throws SQLException {
        List<String> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT role_code FROM roles ORDER BY role_code");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) result.add(results.getString(1));
        }
        return result;
    }

    private Set<String> permissionCodes(Connection connection) throws SQLException {
        Set<String> result = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT permission_code FROM permissions ORDER BY permission_code");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) result.add(results.getString(1));
        }
        return result;
    }

    private Set<String> selectedPermissions(Connection connection, String roleCode) throws SQLException {
        Set<String> result = new HashSet<>();
        String sql = "SELECT p.permission_code FROM role_permissions rp "
                + "JOIN roles r ON r.id=rp.role_id JOIN permissions p ON p.id=rp.permission_id "
                + "WHERE r.role_code=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roleCode);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) result.add(results.getString(1));
            }
        }
        return result;
    }

    private int findRoleId(Connection connection, String roleCode) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM roles WHERE role_code=?")) {
            statement.setString(1, roleCode);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) return results.getInt(1);
            }
        }
        throw new IllegalArgumentException("Vai trò không hợp lệ.");
    }

    private boolean emailExists(Connection connection, String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1")) {
            statement.setString(1, email);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
