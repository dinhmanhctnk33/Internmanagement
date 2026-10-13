package com.example.internmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/** Database operations used to create in-application notifications. */
public class NotificationDAO {

    public List<Map<String,Object>> findRecent(int userId,int limit)throws SQLException{
        String sql="SELECT id,title,message,target_url targetUrl,is_read `read`,created_at createdAt FROM notifications WHERE user_id=? ORDER BY created_at DESC,id DESC LIMIT ?";
        List<Map<String,Object>> out=new ArrayList<>();try(Connection c=com.example.internmanagement.util.DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,userId);s.setInt(2,limit);try(ResultSet r=s.executeQuery()){while(r.next()){Map<String,Object> row=new LinkedHashMap<>();row.put("id",r.getLong("id"));row.put("title",r.getString("title"));row.put("message",r.getString("message"));row.put("targetUrl",r.getString("targetUrl"));row.put("read",r.getBoolean("read"));row.put("createdAt",r.getTimestamp("createdAt"));out.add(row);}}}return out;
    }
    public int unreadCount(int userId)throws SQLException{try(Connection c=com.example.internmanagement.util.DBConnection.getConnection();PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0")){s.setInt(1,userId);try(ResultSet r=s.executeQuery()){r.next();return r.getInt(1);}}}
    public void markAllRead(int userId)throws SQLException{try(Connection c=com.example.internmanagement.util.DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE notifications SET is_read=1 WHERE user_id=?")){s.setInt(1,userId);s.executeUpdate();}}

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
