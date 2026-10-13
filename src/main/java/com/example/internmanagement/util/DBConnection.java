package com.example.internmanagement.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DBConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final Map<String, String> jsonConfig = loadJsonConfig("db-config.json");

    // 1. Đọc file config.json từ thư mục resources
    private static Map<String, String> loadJsonConfig(String fileName) {
        Map<String, String> config = new HashMap<>();
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                return config; // Trả về map rỗng nếu không tìm thấy file json
            }
            String content = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining());

            // Tách các cặp "key": "value" bằng Regex đơn giản
            Matcher matcher = Pattern.compile("\"(.*?)\"\\s*:\\s*\"(.*?)\"").matcher(content);
            while (matcher.find()) {
                config.put(matcher.group(1), matcher.group(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    // 2. Lấy giá trị: Ưu tiên lấy từ biến môi trường, nếu không có thì lấy từ file JSON
    private static String requiredEnvironmentVariable(String name) throws SQLException {
        // Lấy từ System Environment trước
        String value = System.getenv(name);
        
        // Nếu không có trong System Environment thì lấy từ file db-config.json
        if (value == null || value.isBlank()) {
            value = jsonConfig.get(name);
        }

        if (value == null || value.isBlank()) {
            throw new SQLException("Required environment variable or JSON config for " + name + " is not set");
        }
        return value;
    }

    // 3. Hàm mở kết nối DB
    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(
                    requiredEnvironmentVariable("IMS_DB_URL"),
                    requiredEnvironmentVariable("IMS_DB_USER"),
                    requiredEnvironmentVariable("IMS_DB_PASSWORD"));
            ensureDatabaseInitialized(connection);
            return connection;
        } catch (SQLException mysqlException) {
            try {
                Connection h2Connection = DriverManager.getConnection(
                        "jdbc:h2:mem:internship_management;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                        "sa",
                        "");
                ensureDatabaseInitialized(h2Connection);
                return h2Connection;
            } catch (SQLException h2Exception) {
                mysqlException.addSuppressed(h2Exception);
                throw mysqlException;
            }
        }
    }

    static synchronized void ensureDatabaseInitialized(Connection connection) throws SQLException {
        if (tableExists(connection, "users")) {
            return;
        }

        if (connection.getMetaData().getURL().startsWith("jdbc:h2:")) {
            initializeEmbeddedDatabase(connection);
            return;
        }

        executeScript(connection, "schema.sql");
        executeScript(connection, "demo-data.sql");
    }

    private static synchronized void initializeEmbeddedDatabase(Connection connection) throws SQLException {
        if (tableExists(connection, "users")) {
            return;
        }

        String[] statements = {
                "CREATE TABLE users (id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL, password VARCHAR(255) NOT NULL, email VARCHAR(100) NOT NULL, full_name VARCHAR(100) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', last_login_at DATETIME NULL, create_at DATE NULL, update_at DATE NULL, c_password BOOLEAN NOT NULL DEFAULT FALSE, act_token VARCHAR(255) NULL, ex_date_at DATE NULL, phone_number VARCHAR(20) NULL)",
                "CREATE TABLE roles (id BIGINT AUTO_INCREMENT PRIMARY KEY, role_code VARCHAR(30) NOT NULL, role_name VARCHAR(100) NOT NULL, description VARCHAR(255) NULL)",
                "CREATE TABLE candidates (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NULL UNIQUE, full_name VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL, phone VARCHAR(20), university_name VARCHAR(150), major_name VARCHAR(100), desired_position VARCHAR(100), cv_file_url VARCHAR(255), application_status VARCHAR(30) NOT NULL DEFAULT 'NEW', hr_notes TEXT, applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_candidates_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",
                "CREATE TABLE permissions (id BIGINT AUTO_INCREMENT PRIMARY KEY, permission_code VARCHAR(50) NOT NULL, permission_name VARCHAR(100) NOT NULL, module_group VARCHAR(50) NULL)",
                "CREATE TABLE role_permissions (role_id BIGINT NOT NULL, permission_id BIGINT NOT NULL, PRIMARY KEY (role_id, permission_id))",
                "CREATE TABLE user_roles (user_id BIGINT NOT NULL, role_id BIGINT NOT NULL, assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (user_id, role_id))",
                "CREATE TABLE departments (id BIGINT AUTO_INCREMENT PRIMARY KEY, dept_code VARCHAR(30) NOT NULL, dept_name VARCHAR(150) NOT NULL, head_name VARCHAR(100) NULL, is_active BOOLEAN NOT NULL DEFAULT TRUE)",
                "CREATE TABLE internship_programs (id BIGINT AUTO_INCREMENT PRIMARY KEY, program_code VARCHAR(50) NOT NULL, program_name VARCHAR(200) NOT NULL, department_id BIGINT NULL, quota_count INT NULL, start_date DATE NULL, end_date DATE NULL, description TEXT NULL, status VARCHAR(30) NOT NULL DEFAULT 'PLANNING')",
                "CREATE TABLE mentors (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, mentor_code VARCHAR(30) NOT NULL, department_id BIGINT NULL, job_title VARCHAR(100) NULL, max_capacity INT NOT NULL DEFAULT 5, status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE')",
                "CREATE TABLE intern_profiles (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, intern_code VARCHAR(30) NOT NULL, identity_number VARCHAR(20) NULL, date_of_birth DATE NULL, gender VARCHAR(10) NULL, address VARCHAR(255) NULL, university_name VARCHAR(150) NULL, major_name VARCHAR(100) NULL, program_id BIGINT NULL, mentor_id BIGINT NULL, start_date DATE NULL, end_date DATE NULL, bank_account_no VARCHAR(30) NULL, bank_name VARCHAR(100) NULL, phone_number VARCHAR(20) NULL, internship_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE')",
                "CREATE TABLE tasks (id BIGINT AUTO_INCREMENT PRIMARY KEY, task_title VARCHAR(200) NOT NULL, description TEXT NULL, mentor_id BIGINT NULL, intern_profile_id BIGINT NULL, due_date DATETIME NULL, priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', completion_percent INT NOT NULL DEFAULT 0, status VARCHAR(30) NOT NULL DEFAULT 'TODO', deliverable_url VARCHAR(500) NULL)",
                "CREATE TABLE intern_documents (id BIGINT AUTO_INCREMENT PRIMARY KEY, intern_profile_id BIGINT NOT NULL, document_type VARCHAR(50) NOT NULL, file_name VARCHAR(255) NULL, file_url VARCHAR(500) NULL, file_size_bytes BIGINT NULL, approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', rejection_note TEXT NULL, uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE notifications (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, title VARCHAR(200) NOT NULL, message TEXT, target_url VARCHAR(255), is_read BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                "INSERT INTO roles (id, role_code, role_name, description) VALUES (1, 'ADMIN', 'Quản trị viên', 'Quản trị hệ thống'), (2, 'HR', 'Phòng nhân sự', 'Quản lý nhân sự và TTS'), (3, 'MENTOR', 'Cán bộ Hướng dẫn', 'Hướng dẫn thực tập sinh'), (4, 'INTERN', 'Thực tập sinh', 'Sinh viên thực tập'), (5, 'CANDIDATE', 'Ứng viên', 'Ứng viên đăng ký chương trình thực tập')",
                "INSERT INTO permissions (id, permission_code, permission_name, module_group) VALUES (1, 'INTERN_CREATE', 'Tạo hồ sơ thực tập sinh', 'INTERN'), (2, 'INTERN_EDIT', 'Chỉnh sửa hồ sơ thực tập sinh', 'INTERN'), (3, 'DOCUMENT_REVIEW', 'Duyệt tài liệu thực tập sinh', 'DOCUMENT'), (4, 'DOCUMENT_UPLOAD', 'Tải tài liệu thực tập sinh', 'DOCUMENT')",
                "INSERT INTO role_permissions (role_id, permission_id) SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.role_code = 'ADMIN' OR (r.role_code = 'HR' AND p.permission_code IN ('INTERN_CREATE', 'INTERN_EDIT', 'DOCUMENT_REVIEW')) OR (r.role_code = 'INTERN' AND p.permission_code = 'DOCUMENT_UPLOAD')",
                "INSERT INTO users (id, username, password, email, full_name, status, create_at, update_at) VALUES (1, 'admin_demo', '123456', 'admin.demo@ims.local', 'Quản trị viên Demo', 'ACTIVE', CURRENT_DATE, CURRENT_DATE), (2, 'hr_demo', '123456', 'hr.demo@ims.local', 'Nhân sự Demo', 'ACTIVE', CURRENT_DATE, CURRENT_DATE), (3, 'mentor_demo', '123456', 'mentor.demo@ims.local', 'Mentor Demo', 'ACTIVE', CURRENT_DATE, CURRENT_DATE), (4, 'intern_demo', '123456', 'intern.demo@ims.local', 'Thực tập sinh Demo', 'ACTIVE', CURRENT_DATE, CURRENT_DATE)",
                "INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u JOIN roles r ON 1 = 1 WHERE (u.username = 'admin_demo' AND r.role_code = 'ADMIN') OR (u.username = 'hr_demo' AND r.role_code = 'HR') OR (u.username = 'mentor_demo' AND r.role_code = 'MENTOR') OR (u.username = 'intern_demo' AND r.role_code = 'INTERN')",
                "INSERT INTO departments (id, dept_code, dept_name, head_name, is_active) VALUES (1, 'IT', 'Phòng Công nghệ Thông tin', 'Quản trị viên Demo', TRUE)",
                "INSERT INTO internship_programs (id, program_code, program_name, department_id, quota_count, start_date, end_date, description, status) VALUES (1, 'TTS-2026-Q1', 'Đợt thực tập 2026 Q1', 1, 20, DATE '2026-01-15', DATE '2026-06-30', 'Đợt thực tập demo cho sinh viên', 'ACTIVE')",
                "INSERT INTO mentors (id, user_id, mentor_code, department_id, job_title, max_capacity, status) VALUES (1, 3, 'MENTOR-001', 1, 'Mentor Backend', 5, 'ACTIVE')",
                "INSERT INTO intern_profiles (id, user_id, intern_code, identity_number, date_of_birth, gender, address, university_name, major_name, program_id, mentor_id, phone_number, internship_status) VALUES (1, 4, 'TTS001', '001200001234', DATE '2002-05-15', 'Nam', 'Hà Nội', 'ĐH Bách Khoa', 'Công nghệ thông tin', 1, 1, '0912345678', 'ACTIVE')",
                "INSERT INTO intern_documents (id, intern_profile_id, document_type, file_name, file_url, file_size_bytes, approval_status, rejection_note, uploaded_at) VALUES (1, 1, 'CV', 'CV_ThucTapSinh_Demo.pdf', '/uploads/demo_cv.pdf', 1048576, 'PENDING', NULL, CURRENT_TIMESTAMP)"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : statements) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
    }

    private static void executeScript(Connection connection, String resourcePath) throws SQLException {
        try (InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new SQLException("Không tìm thấy file script: " + resourcePath);
            }

            String sqlScript = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            String[] statements = sqlScript.split(";");
            try (Statement stmt = connection.createStatement()) {
                for (String sql : statements) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            throw new SQLException("Không thể chạy script " + resourcePath, e);
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(
                connection.getCatalog(),
                connection.getSchema(),
                tableName.toUpperCase(),
                new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }

    // 4. Hàm thực thi file script .sql
    public static void executeSqlScript(String resourcePath) {
        Connection conn = null;
        try {
            try {
                conn = getConnection();
            } catch (SQLException e) {
                conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/",
                        requiredEnvironmentVariable("IMS_DB_USER"),
                        requiredEnvironmentVariable("IMS_DB_PASSWORD"));
            }

            try (InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    System.err.println("Không tìm thấy file script: " + resourcePath);
                    return;
                }

                String sqlScript = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));

                String[] statements = sqlScript.split(";");

                try (Statement stmt = conn.createStatement()) {
                    for (String sql : statements) {
                        if (!sql.trim().isEmpty()) {
                            stmt.execute(sql.trim());
                        }
                    }
                }
                System.out.println("Thực thi script thành công: " + resourcePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // 5. Hàm main test
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", requiredEnvironmentVariable("IMS_DB_USER"), requiredEnvironmentVariable("IMS_DB_PASSWORD"));
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP DATABASE IF EXISTS internship_management");
            System.out.println("Đã xoá database cũ (nếu có)");
        } catch (Exception e) {
            e.printStackTrace();
        }
        executeSqlScript("schema.sql");
        executeSqlScript("demo-data.sql");
    }
}