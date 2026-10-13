package com.example.internmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationDAO {

    public int notifyRole(Connection connection, String roleCode, String title, String message, String targetUrl)
            throws SQLException {
        String sql = """
                INSERT INTO notifications (user_id, title, message, target_url, is_read, created_at)
                SELECT u.id, ?, ?, ?, FALSE, CURRENT_TIMESTAMP
                FROM roles r
                JOIN user_roles ur ON ur.role_id = r.id
                JOIN users u ON u.id = ur.user_id
                WHERE r.role_code = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, message);
            ps.setString(3, targetUrl);
            ps.setString(4, roleCode);
            return ps.executeUpdate();
        }
    }

    public int notifyDocumentOwner(Connection connection, int documentId, String title, String message, String targetUrl)
            throws SQLException {
        Integer profileId = null;
        String getProfileSql = "SELECT intern_profile_id FROM intern_documents WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(getProfileSql)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    profileId = rs.getInt("intern_profile_id");
                }
            }
        }

        if (profileId == null) {
            return 0;
        }

        Integer userId = null;
        String getUserSql = "SELECT user_id FROM intern_profiles WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(getUserSql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                }
            }
        }

        if (userId == null) {
            return 0;
        }

        String sql = "INSERT INTO notifications (user_id, title, message, target_url, is_read, created_at) VALUES (?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, targetUrl);
            return ps.executeUpdate();
        }
    }
}
