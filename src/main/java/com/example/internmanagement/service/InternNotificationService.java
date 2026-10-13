package com.example.internmanagement.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.internmanagement.dao.IInternshipContextDAO;
import com.example.internmanagement.dao.INotificationDAO;
import com.example.internmanagement.dao.JdbcInternshipContextDAO;
import com.example.internmanagement.model.InAppNotification;
import com.example.internmanagement.model.Notification;

public class InternNotificationService {
    private final INotificationDAO notificationDAO;
    private final IInternshipContextDAO contextDAO;

    public InternNotificationService(INotificationDAO notificationDAO) {
        this(notificationDAO, new JdbcInternshipContextDAO());
    }

    public InternNotificationService(INotificationDAO notificationDAO, IInternshipContextDAO contextDAO) {
        this.notificationDAO = notificationDAO;
        this.contextDAO = contextDAO;
    }

    public String getUnreadBadgeDisplay(Connection connection, int currentInternId) throws SQLException {
        int unreadCount = notificationDAO.countUnread(connection, currentInternId);
        if (unreadCount <= 0) {
            return "0";
        }
        return unreadCount > 99 ? "99+" : Integer.toString(unreadCount);
    }

    public List<Notification> getNotifications(Connection connection, int currentInternId,
                                                boolean unreadOnly, int pageIndex, int pageSize)
            throws SQLException {
        if (currentInternId <= 0) {
            return List.of();
        }
        List<InAppNotification> notifications = notificationDAO.findAllByRecipient(
            connection, currentInternId, unreadOnly, pageIndex, pageSize);
        return new ArrayList<>(notifications);
    }

    public boolean markAsRead(Connection connection, int currentInternId, long notificationId)
            throws SQLException {
        if (currentInternId <= 0 || notificationId <= 0) {
            return false;
        }
        return notificationDAO.markAsRead(connection, currentInternId, notificationId);
    }

    public void markAllAsRead(Connection connection, int currentInternId) throws SQLException {
        if (currentInternId > 0) {
            notificationDAO.markAllAsRead(connection, currentInternId);
        }
    }

    public ClickResult handleClickAndNavigate(Connection connection, int currentInternId, long notificationId)
            throws SQLException {
        Notification notification = notificationDAO.findByIdForRecipient(connection, currentInternId, notificationId);
        if (notification == null) {
            return new ClickResult(false, null, "Thông báo không tồn tại hoặc bạn không có quyền truy cập.");
        }

        notificationDAO.markAsRead(connection, currentInternId, notificationId);
        String entityType = notification.getTargetEntityType();
        Long entityId = notification.getTargetEntityId();
        if ("Meeting".equals(entityType)) {
            if (entityId == null || !contextDAO.isMeetingActiveAndAccessible(connection, entityId, currentInternId)) {
                return new ClickResult(false, null, "Nội dung cuộc họp chưa khả dụng trong hệ thống.");
            }
        }
        if ("Task".equals(entityType) && (entityId == null
                || !contextDAO.isTaskActiveAndAssigned(connection, entityId, currentInternId))) {
            return new ClickResult(false, null, "Nội dung không còn khả dụng.");
        }

        String targetUrl = notification.getTargetUrl();
        if (targetUrl == null || !targetUrl.startsWith("/") || targetUrl.startsWith("//")
                || targetUrl.contains("\\") || targetUrl.contains("\r") || targetUrl.contains("\n")) {
            return new ClickResult(false, null, "Đường dẫn thông báo không hợp lệ.");
        }
        return new ClickResult(true, targetUrl, null);
    }

    public record ClickResult(boolean success, String targetUrl, String errorMessage) {
    }
}