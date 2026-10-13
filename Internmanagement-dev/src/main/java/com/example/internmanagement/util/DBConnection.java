package com.example.internmanagement.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
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
        return DriverManager.getConnection(
                requiredEnvironmentVariable("IMS_DB_URL"),
                requiredEnvironmentVariable("IMS_DB_USER"),
                requiredEnvironmentVariable("IMS_DB_PASSWORD"));
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