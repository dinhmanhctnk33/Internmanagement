package com.example.internmanagement.dao;

import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import com.example.internmanagement.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ============================================================
    // 1. ĐĂNG NHẬP
    // ============================================================

    /**
     * Đăng nhập bằng username + password.
     *
     * Chỉ tài khoản có status = ACTIVE mới được đăng nhập.
     */
    public User login(String identifier, String password) {

        String sql = """
                SELECT
                    id,
                    username,
                    password,
                    email,
                    full_name,
                    status,
                    last_login_at,
                    create_at,
                    update_at,
                    c_password,
                    act_token,
                    ex_date_at,
                    phone_number
                FROM users
                WHERE (username = ? OR LOWER(email) = LOWER(?))
                  AND status = 'ACTIVE'
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, identifier.trim());
            statement.setString(2, identifier.trim());
            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (!PasswordUtil.matches(password, storedPassword)) return null;

                    User user = mapUser(rs);
                    if (PasswordUtil.needsUpgrade(storedPassword)) {
                        updatePassword(user.getId(), password);
                        user.setPassword(PasswordUtil.hash(password));
                    }

                    // Cập nhật thời gian đăng nhập
                    updateLastLogin(user.getId());

                    // Cập nhật lại object
                    user.setLast_login_at(LocalDateTime.now());

                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // ============================================================
    // 2. TÌM USER THEO ID
    // ============================================================

    public User findById(int id) {

        String sql = """
                SELECT
                    id,
                    username,
                    password,
                    email,
                    full_name,
                    status,
                    last_login_at,
                    create_at,
                    update_at,
                    c_password,
                    act_token,
                    ex_date_at,
                    phone_number
                FROM users
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return mapUser(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // ============================================================
    // 3. TÌM USER THEO USERNAME
    // ============================================================

    public User findByUsername(String username) {

        String sql = """
                SELECT
                    id,
                    username,
                    password,
                    email,
                    full_name,
                    status,
                    last_login_at,
                    create_at,
                    update_at,
                    c_password,
                    act_token,
                    ex_date_at,
                    phone_number
                FROM users
                WHERE username = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return mapUser(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // ============================================================
    // 4. TÌM USER THEO EMAIL
    // ============================================================

    public User findByEmail(String email) {

        String sql = """
                SELECT
                    id,
                    username,
                    password,
                    email,
                    full_name,
                    status,
                    last_login_at,
                    create_at,
                    update_at,
                    c_password,
                    act_token,
                    ex_date_at,
                    phone_number
                FROM users
                WHERE email = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return mapUser(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // ============================================================
    // 5. LẤY TẤT CẢ USER
    // ============================================================

    public List<User> findAll() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    username,
                    password,
                    email,
                    full_name,
                    status,
                    last_login_at,
                    create_at,
                    update_at,
                    c_password,
                    act_token,
                    ex_date_at,
                    phone_number
                FROM users
                ORDER BY id DESC
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet rs =
                        statement.executeQuery()
        ) {

            while (rs.next()) {

                User user = mapUser(rs);

                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }


    // ============================================================
    // 6. KIỂM TRA USERNAME
    // ============================================================

    public boolean existsByUsername(String username) {

        String sql = """
                SELECT 1
                FROM users
                WHERE username = ?
                LIMIT 1
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 7. KIỂM TRA EMAIL
    // ============================================================

    public boolean existsByEmail(String email) {

        String sql = """
                SELECT 1
                FROM users
                WHERE email = ?
                LIMIT 1
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 8. THÊM USER
    // ============================================================

    public boolean insert(User user) {

        String sql = """
                INSERT INTO users
                (
                    username,
                    password,
                    email,
                    full_name,
                    status,
                    last_login_at,
                    create_at,
                    update_at,
                    c_password,
                    act_token,
                    ex_date_at,
                    phone_number
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    user.getUsername()
            );

            statement.setString(
                    2,
                    user.getPassword()
            );

            statement.setString(
                    3,
                    user.getEmail()
            );

            statement.setString(
                    4,
                    user.getFull_name()
            );

            statement.setString(
                    5,
                    user.getStatus()
            );

            setLocalDateTime(
                    statement,
                    6,
                    user.getLast_login_at()
            );

            setLocalDate(
                    statement,
                    7,
                    user.getCreate_at()
            );

            setLocalDate(
                    statement,
                    8,
                    user.getUpdate_at()
            );

            statement.setBoolean(
                    9,
                    user.isC_password()
            );

            statement.setString(
                    10,
                    user.getAct_token()
            );

            setLocalDate(
                    statement,
                    11,
                    user.getEx_date_at()
            );

            statement.setString(
                    12,
                    user.getPhone_number()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 9. CẬP NHẬT USER
    // ============================================================

    public boolean update(User user) {

        String sql = """
                UPDATE users
                SET
                    username = ?,
                    email = ?,
                    full_name = ?,
                    status = ?,
                    update_at = ?,
                    c_password = ?,
                    act_token = ?,
                    ex_date_at = ?,
                    phone_number = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    user.getUsername()
            );

            statement.setString(
                    2,
                    user.getEmail()
            );

            statement.setString(
                    3,
                    user.getFull_name()
            );

            statement.setString(
                    4,
                    user.getStatus()
            );

            setLocalDate(
                    statement,
                    5,
                    user.getUpdate_at()
            );

            statement.setBoolean(
                    6,
                    user.isC_password()
            );

            statement.setString(
                    7,
                    user.getAct_token()
            );

            setLocalDate(
                    statement,
                    8,
                    user.getEx_date_at()
            );

            statement.setString(
                    9,
                    user.getPhone_number()
            );

            statement.setInt(
                    10,
                    user.getId()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 10. ĐỔI PASSWORD
    // ============================================================

    public boolean updatePassword(
            int userId,
            String newPassword) {

        String sql = """
                UPDATE users
                SET
                    password = ?,
                    c_password = true,
                    update_at = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    PasswordUtil.hash(newPassword)
            );

            statement.setDate(
                    2,
                    Date.valueOf(LocalDate.now())
            );

            statement.setInt(
                    3,
                    userId
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 11. CẬP NHẬT THỜI GIAN ĐĂNG NHẬP
    // ============================================================

    public boolean updateLastLogin(int userId) {

        String sql = """
                UPDATE users
                SET last_login_at = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(LocalDateTime.now())
            );

            statement.setInt(
                    2,
                    userId
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 12. CẬP NHẬT STATUS
    // ============================================================

    public boolean updateStatus(
            int userId,
            String status) {

        String sql = """
                UPDATE users
                SET
                    status = ?,
                    update_at = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    status
            );

            statement.setDate(
                    2,
                    Date.valueOf(LocalDate.now())
            );

            statement.setInt(
                    3,
                    userId
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 13. XÓA USER
    // ============================================================

    public boolean delete(int userId) {

        String sql = """
                DELETE FROM users
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 14. TÌM USER THEO TOKEN
    // ============================================================

    /**
     * Dùng cho các chức năng như:
     *
     * - Xác thực tài khoản
     * - Quên mật khẩu
     * - Reset password
     *
     * tùy nghiệp vụ sau này.
     */
    public User findByActivationToken(String token) {

        String sql = """
                SELECT
                    id,
                    username,
                    password,
                    email,
                    full_name,
                    status,
                    last_login_at,
                    create_at,
                    update_at,
                    c_password,
                    act_token,
                    ex_date_at,
                    phone_number
                FROM users
                WHERE act_token = ?
                  AND ex_date_at >= CURDATE()
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, token);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return mapUser(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // ============================================================
    // 15. CẬP NHẬT TOKEN
    // ============================================================

    public boolean updateActivationToken(
            int userId,
            String token,
            LocalDate expireDate) {

        String sql = """
                UPDATE users
                SET
                    act_token = ?,
                    ex_date_at = ?,
                    update_at = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    token
            );

            setLocalDate(
                    statement,
                    2,
                    expireDate
            );

            statement.setDate(
                    3,
                    Date.valueOf(LocalDate.now())
            );

            statement.setInt(
                    4,
                    userId
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 16. XÓA TOKEN
    // ============================================================

    public boolean clearActivationToken(int userId) {

        String sql = """
                UPDATE users
                SET
                    act_token = NULL,
                    ex_date_at = NULL,
                    update_at = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setDate(
                    1,
                    Date.valueOf(LocalDate.now())
            );

            statement.setInt(
                    2,
                    userId
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ============================================================
    // 17. MAP RESULTSET -> USER
    // ============================================================

    private User mapUser(ResultSet rs)
            throws SQLException {

        User user = new User();

        user.setId(
                rs.getInt("id")
        );

        user.setUsername(
                rs.getString("username")
        );

        user.setPassword(
                rs.getString("password")
        );

        user.setEmail(
                rs.getString("email")
        );

        user.setFull_name(
                rs.getString("full_name")
        );

        user.setStatus(
                rs.getString("status")
        );

        // --------------------------------------------------------
        // last_login_at
        // --------------------------------------------------------

        Timestamp lastLogin =
                rs.getTimestamp("last_login_at");

        if (lastLogin != null) {
            user.setLast_login_at(
                    lastLogin.toLocalDateTime()
            );
        }

        // --------------------------------------------------------
        // create_at
        // --------------------------------------------------------

        Date createDate =
                rs.getDate("create_at");

        if (createDate != null) {
            user.setCreate_at(
                    createDate.toLocalDate()
            );
        }

        // --------------------------------------------------------
        // update_at
        // --------------------------------------------------------

        Date updateDate =
                rs.getDate("update_at");

        if (updateDate != null) {
            user.setUpdate_at(
                    updateDate.toLocalDate()
            );
        }

        // --------------------------------------------------------
        // c_password
        // --------------------------------------------------------

        user.setC_password(
                rs.getBoolean("c_password")
        );

        // --------------------------------------------------------
        // act_token
        // --------------------------------------------------------

        user.setAct_token(
                rs.getString("act_token")
        );

        // --------------------------------------------------------
        // ex_date_at
        // --------------------------------------------------------

        Date expireDate =
                rs.getDate("ex_date_at");

        if (expireDate != null) {
            user.setEx_date_at(
                    expireDate.toLocalDate()
            );
        }

        // --------------------------------------------------------
        // phone_number
        // --------------------------------------------------------

        user.setPhone_number(
                rs.getString("phone_number")
        );

        return user;
    }


    // ============================================================
    // 18. HELPER - LOCALDATETIME -> SQL TIMESTAMP
    // ============================================================

    private void setLocalDateTime(
            PreparedStatement statement,
            int index,
            LocalDateTime value)
            throws SQLException {

        if (value != null) {

            statement.setTimestamp(
                    index,
                    Timestamp.valueOf(value)
            );

        } else {

            statement.setNull(
                    index,
                    java.sql.Types.TIMESTAMP
            );
        }
    }


    // ============================================================
    // 19. HELPER - LOCALDATE -> SQL DATE
    // ============================================================

    private void setLocalDate(
            PreparedStatement statement,
            int index,
            LocalDate value)
            throws SQLException {

        if (value != null) {

            statement.setDate(
                    index,
                    Date.valueOf(value)
            );

        } else {

            statement.setNull(
                    index,
                    java.sql.Types.DATE
            );
        }
    }

        public boolean updateResetToken(
                String email,
                String token) {

        String sql = """
                UPDATE users
                SET act_token = ?,
                        ex_date_at = DATE_ADD(CURDATE(), INTERVAL 1 DAY),
                        update_at = CURDATE()
                WHERE email = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

                ps.setString(1, token);
                ps.setString(2, email);

                return ps.executeUpdate() > 0;

        } catch (Exception e) {

                e.printStackTrace();

        }

        return false;
        }

    /** Cập nhật mật khẩu sau khi token reset còn hiệu lực và xoá token để không tái sử dụng. */
    public boolean resetPassword(String token, String newPassword) {
        String sql = """
                UPDATE users
                SET password = ?,
                    c_password = TRUE,
                    act_token = NULL,
                    ex_date_at = NULL,
                    update_at = CURDATE()
                WHERE act_token = ?
                  AND ex_date_at >= CURDATE()
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setString(2, token);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserRoleCode(int userId) {
        String sql = """
                SELECT r.role_code
                FROM roles r
                JOIN user_roles ur ON ur.role_id = r.id
                WHERE ur.user_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role_code");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "INTERN";
    }
}

