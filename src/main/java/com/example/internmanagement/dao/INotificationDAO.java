package com.example.internmanagement.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.example.internmanagement.model.InAppNotification;

public interface INotificationDAO {
    int insert(Connection connection, InAppNotification notification) throws SQLException;

    int update(Connection connection, InAppNotification notification) throws SQLException;

    InAppNotification findById(Connection connection, long id) throws SQLException;

    InAppNotification findByIdForRecipient(Connection connection, int recipientId, long id) throws SQLException;

    List<InAppNotification> findAllByRecipient(Connection connection, int recipientId, boolean unreadOnly,
                                                int pageIndex, int pageSize) throws SQLException;

    int countUnread(Connection connection, int recipientId) throws SQLException;

    boolean markAsRead(Connection connection, int recipientId, long id) throws SQLException;

    void markAllAsRead(Connection connection, int recipientId) throws SQLException;

    boolean existsByReminderKey(Connection connection, int recipientId, String reminderKey) throws SQLException;

    boolean isTaskActiveAndAssigned(Connection connection, long taskId, int recipientId) throws SQLException;

    int deleteOlderThan(Connection connection, LocalDateTime cutoffDate) throws SQLException;
}