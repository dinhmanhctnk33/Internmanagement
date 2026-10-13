package com.example.internmanagement.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.InAppNotification;
import com.example.internmanagement.model.NotificationType;

public class NotificationEventService {
    private static final Logger LOGGER = Logger.getLogger(NotificationEventService.class.getName());

    private final NotificationDAO notificationDAO;

    public NotificationEventService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public void processSingleEvent(Connection connection, int recipientId, NotificationType type,
                                   String title, String rawContent, String targetUrl,
                                   String targetEntityType, Long targetEntityId) {
        try {
            InAppNotification notification = new InAppNotification();
            notification.setRecipientId(recipientId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setShortContent(sanitizeContent(type, rawContent));
            notification.setTargetUrl(targetUrl);
            notification.setTargetEntityType(targetEntityType);
            notification.setTargetEntityId(targetEntityId);
            notificationDAO.notifyUser(connection, notification);
        } catch (SQLException | RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tạo thông báo in-app cho thực tập sinh " + recipientId, exception);
        }
    }

    public void processBatchEvents(Connection connection, int recipientId, NotificationType type,
                                   int itemCount, String summaryTemplate, String targetUrl) {
        if (recipientId <= 0 || itemCount <= 0) {
            return;
        }
        try {
            String content = itemCount > 1 ? itemCount + " " + summaryTemplate : summaryTemplate;
            InAppNotification notification = new InAppNotification();
            notification.setRecipientId(recipientId);
            notification.setType(type);
            notification.setTitle("Cập nhật công việc");
            notification.setShortContent(content);
            notification.setTargetUrl(targetUrl);
            notificationDAO.notifyUser(connection, notification);
        } catch (SQLException | RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Lỗi khi gộp thông báo batch cho thực tập sinh " + recipientId, exception);
        }
    }

    public void processAdmissionResult(Connection connection, int candidateId, boolean emailDispatched) {
        if (!emailDispatched || candidateId <= 0) {
            return;
        }
        try {
            InAppNotification notification = new InAppNotification();
            notification.setRecipientId(candidateId);
            notification.setType(NotificationType.ADMISSION_RESULT_RELEASED);
            notification.setTitle("Kết quả xét duyệt hồ sơ");
            notification.setShortContent("Kết quả xét duyệt hồ sơ thực tập của bạn đã được công bố.");
            notification.setTargetUrl("/applications");
            notificationDAO.notifyUser(connection, notification);
        } catch (SQLException | RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tạo thông báo kết quả xét duyệt cho ứng viên " + candidateId,
                    exception);
        }
    }

    private String sanitizeContent(NotificationType type, String originalContent) {
        if (type == NotificationType.FINAL_EVALUATION_COMPLETED) {
            return "Đánh giá tổng kết của bạn đã sẵn sàng.";
        }
        return originalContent;
    }
}