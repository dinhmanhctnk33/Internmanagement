package com.example.internmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Database operations used to create in-application notifications. */
public class NotificationDAO {

    public int notifyRole(Connection connection, String roleCode, String title,
                          String message, String targetUrl) throws SQLException {
        String sql = """
                INSERT INTO notifications (user_id, title, message, target_url)
                SELECT DISTINCT ur.user_id, ?, ?, ?
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                JOIN users u ON u.id = ur.user_id
                WHERE r.role_code = ? AND u.status = 'ACTIVE'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, message);
            statement.setString(3, targetUrl);
            statement.setString(4, roleCode);
            return statement.executeUpdate();
        }
    }

    public int notifyDocumentOwner(Connection connection, int documentId, String title,
                                   String message, String targetUrl) throws SQLException {
        String ownerSql = """
                SELECT ip.user_id
                FROM intern_documents d
                JOIN intern_profiles ip ON ip.id = d.intern_profile_id
                WHERE d.id = ?
                """;
        Integer userId = null;
        try (PreparedStatement statement = connection.prepareStatement(ownerSql)) {
            statement.setInt(1, documentId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    userId = result.getInt(1);
                }
            }
        }
        if (userId == null) {
            return 0;
        }

        String insertSql = "INSERT INTO notifications (user_id, title, message, target_url) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setInt(1, userId);
            statement.setString(2, title);
            statement.setString(3, message);
            statement.setString(4, targetUrl);
            return statement.executeUpdate();
        }
    }
}
