package com.example.internmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.internmanagement.model.InAppNotification;
import com.example.internmanagement.model.Notification;
import com.example.internmanagement.model.NotificationType;

public class NotificationDAO implements INotificationDAO {

    @Override
    public int insert(Connection connection, InAppNotification notification) throws SQLException {
        return notifyUser(connection, notification);
    }

    @Override
    public int update(Connection connection, InAppNotification notification) throws SQLException {
        if (notification == null || notification.getId() <= 0 || notification.getUserId() <= 0) {
            return 0;
        }
        String sql = "UPDATE notifications SET notification_type = ?, title = ?, message = ?, target_url = ?, "
                + "target_entity_type = ?, target_entity_id = ?, reminder_key = ?, is_read = ?, created_at = ? "
                + "WHERE id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (notification.getType() == null) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, notification.getType().name());
            }
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getTargetUrl());
            ps.setString(5, notification.getTargetEntityType());
            if (notification.getTargetEntityId() == null) {
                ps.setNull(6, Types.BIGINT);
            } else {
                ps.setLong(6, notification.getTargetEntityId());
            }
            ps.setString(7, notification.getReminderKey());
            ps.setBoolean(8, notification.isRead());
            LocalDateTime createdAt = notification.getCreatedAt() == null
                    ? LocalDateTime.now() : notification.getCreatedAt();
            ps.setTimestamp(9, Timestamp.valueOf(createdAt));
            ps.setLong(10, notification.getId());
            ps.setInt(11, notification.getUserId());
            return ps.executeUpdate();
        }
    }

    @Override
    public InAppNotification findById(Connection connection, long id) throws SQLException {
        String sql = "SELECT id, user_id, notification_type, title, message, target_url, target_entity_type, "
                + "target_entity_id, reminder_key, is_read, created_at FROM notifications WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapNotification(rs) : null;
            }
        }
    }

    @Override
    public InAppNotification findByIdForRecipient(Connection connection, int recipientId, long id)
            throws SQLException {
        return findByIdForUser(connection, recipientId, id);
    }

    @Override
    public List<InAppNotification> findAllByRecipient(Connection connection, int recipientId,
                                                       boolean unreadOnly, int pageIndex, int pageSize)
            throws SQLException {
        List<InAppNotification> results = new ArrayList<>();
        for (Notification notification : findPageByUserId(
                connection, recipientId, unreadOnly, pageIndex, pageSize)) {
            results.add((InAppNotification) notification);
        }
        return results;
    }

    @Override
    public int countUnread(Connection connection, int recipientId) throws SQLException {
        return countUnreadForUser(connection, recipientId);
    }

    @Override
    public boolean markAsRead(Connection connection, int recipientId, long id) throws SQLException {
        return markReadForUser(connection, recipientId, id);
    }

    @Override
    public void markAllAsRead(Connection connection, int recipientId) throws SQLException {
        markAllRead(connection, recipientId);
    }

    @Override
    public boolean existsByReminderKey(Connection connection, int recipientId, String reminderKey)
            throws SQLException {
        return reminderKey != null && notificationExists(connection, recipientId, reminderKey);
    }

    @Override
    public int deleteOlderThan(Connection connection, LocalDateTime cutoffDate) throws SQLException {
        String sql = "DELETE FROM notifications WHERE created_at < ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(cutoffDate));
            return ps.executeUpdate();
        }
    }

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

    public int notifyUser(Connection connection, int userId, String title, String message, String targetUrl)
            throws SQLException {
        if (userId <= 0) {
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

    public int notifyUser(Connection connection, Notification notification) throws SQLException {
        if (notification == null || notification.getUserId() <= 0) {
            return 0;
        }
        String sql = "INSERT INTO notifications (user_id, notification_type, title, message, target_url, "
                + "target_entity_type, target_entity_id, reminder_key, is_read, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getUserId());
            if (notification.getType() == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, notification.getType().name());
            }
            ps.setString(3, notification.getTitle());
            ps.setString(4, notification.getMessage());
            ps.setString(5, notification.getTargetUrl());
            ps.setString(6, notification.getTargetEntityType());
            if (notification.getTargetEntityId() == null) {
                ps.setNull(7, Types.BIGINT);
            } else {
                ps.setLong(7, notification.getTargetEntityId());
            }
            ps.setString(8, notification.getReminderKey());
            ps.setBoolean(9, notification.isRead());
            LocalDateTime createdAt = notification.getCreatedAt() == null
                    ? LocalDateTime.now() : notification.getCreatedAt();
            ps.setTimestamp(10, Timestamp.valueOf(createdAt));
            int inserted = ps.executeUpdate();
            if (inserted > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        notification.setId(keys.getLong(1));
                    }
                }
            }
            return inserted;
        } catch (SQLException exception) {
            if (notification.getReminderKey() != null
                    && notificationExists(connection, notification.getUserId(), notification.getReminderKey())) {
                return 0;
            }
            throw exception;
        }
    }

    public int notifyInternProfile(Connection connection, int internProfileId, String title, String message, String targetUrl)
            throws SQLException {
        Integer userId = findUserIdByInternProfile(connection, internProfileId);
        if (userId == null) {
            return 0;
        }
        return notifyUser(connection, userId, title, message, targetUrl);
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

        Integer userId = findUserIdByInternProfile(connection, profileId);
        if (userId == null) {
            return 0;
        }

        return notifyUser(connection, userId, title, message, targetUrl);
    }

    public List<Notification> findByUserId(Connection connection, int userId, int limit) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT id, user_id, notification_type, title, message, target_url, target_entity_type, "
            + "target_entity_id, reminder_key, is_read, created_at FROM notifications "
            + "WHERE user_id = ? ORDER BY created_at DESC, id DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit <= 0 ? 20 : limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification item = new Notification();
                    item.setId(rs.getLong("id"));
                    item.setUserId(rs.getInt("user_id"));
                    String type = rs.getString("notification_type");
                    if (type != null) {
                        item.setType(NotificationType.valueOf(type));
                    }
                    item.setTitle(rs.getString("title"));
                    item.setMessage(rs.getString("message"));
                    item.setTargetUrl(rs.getString("target_url"));
                    item.setTargetEntityType(rs.getString("target_entity_type"));
                    long targetEntityId = rs.getLong("target_entity_id");
                    if (!rs.wasNull()) {
                        item.setTargetEntityId(targetEntityId);
                    }
                    item.setReminderKey(rs.getString("reminder_key"));
                    item.setRead(rs.getBoolean("is_read"));
                    item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    notifications.add(item);
                }
            }
        }
        return notifications;
    }

    public List<Notification> findPageByUserId(Connection connection, int userId, boolean unreadOnly,
                                               int pageIndex, int pageSize) throws SQLException {
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int safePageIndex = Math.max(0, pageIndex);
        long offset = (long) safePageIndex * safePageSize;
        String sql = "SELECT id, user_id, notification_type, title, message, target_url, target_entity_type, "
                + "target_entity_id, reminder_key, is_read, created_at FROM notifications "
                + "WHERE user_id = ?" + (unreadOnly ? " AND is_read = FALSE" : "")
                + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        List<Notification> notifications = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, safePageSize);
            ps.setLong(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapNotification(rs));
                }
            }
        }
        return notifications;
    }

    public InAppNotification findByIdForUser(Connection connection, int userId, long notificationId)
            throws SQLException {
        String sql = "SELECT id, user_id, notification_type, title, message, target_url, target_entity_type, "
                + "target_entity_id, reminder_key, is_read, created_at FROM notifications "
                + "WHERE id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapNotification(rs) : null;
            }
        }
    }

    public boolean markReadForUser(Connection connection, int userId, long notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ? AND is_read = FALSE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            ps.setInt(2, userId);
            if (ps.executeUpdate() > 0) {
                return true;
            }
        }
        return findByIdForUser(connection, userId, notificationId) != null;
    }

    @Override
    public boolean isTaskActiveAndAssigned(Connection connection, long taskId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM tasks t JOIN intern_profiles ip ON ip.id = t.intern_profile_id "
                + "WHERE t.id = ? AND ip.user_id = ? AND ip.internship_status = 'ACTIVE' "
                + "AND t.status NOT IN ('COMPLETED', 'VERIFIED')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, taskId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countUnreadForUser(Connection connection, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void markAllRead(Connection connection, int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private Integer findUserIdByInternProfile(Connection connection, int internProfileId) throws SQLException {
        String sql = "SELECT user_id FROM intern_profiles WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, internProfileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        return null;
    }

    private InAppNotification mapNotification(ResultSet rs) throws SQLException {
        InAppNotification item = new InAppNotification();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getInt("user_id"));
        String type = rs.getString("notification_type");
        if (type != null) {
            item.setType(NotificationType.valueOf(type));
        }
        item.setTitle(rs.getString("title"));
        item.setMessage(rs.getString("message"));
        item.setTargetUrl(rs.getString("target_url"));
        item.setTargetEntityType(rs.getString("target_entity_type"));
        long targetEntityId = rs.getLong("target_entity_id");
        if (!rs.wasNull()) {
            item.setTargetEntityId(targetEntityId);
        }
        item.setReminderKey(rs.getString("reminder_key"));
        item.setRead(rs.getBoolean("is_read"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        return item;
    }

    private boolean notificationExists(Connection connection, int userId, String reminderKey) throws SQLException {
        String sql = "SELECT 1 FROM notifications WHERE user_id = ? AND reminder_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, reminderKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
