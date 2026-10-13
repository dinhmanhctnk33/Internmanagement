package com.example.internmanagement.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.Notification;
import com.example.internmanagement.model.User;
import com.example.internmanagement.service.InternNotificationService;
import com.example.internmanagement.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/notifications")
public class NotificationApiServlet extends HttpServlet {
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final InternNotificationService notificationService =
            new InternNotificationService(notificationDAO);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Integer internId = getAuthenticatedInternId(request);
        if (internId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");
        try (Connection connection = DBConnection.getConnection()) {
            if ("badge".equalsIgnoreCase(action)) {
                String badge = notificationService.getUnreadBadgeDisplay(connection, internId);
                response.getWriter().write("{\"badgeDisplay\":" + jsonString(badge) + "}");
                return;
            }

            boolean unreadOnly = Boolean.parseBoolean(request.getParameter("unreadOnly"));
            int page = parseBoundedInt(request.getParameter("page"), 1, 1, Integer.MAX_VALUE);
            int pageSize = parseBoundedInt(request.getParameter("pageSize"), 20, 1, 100);
            List<Notification> notifications = notificationService.getNotifications(
                    connection, internId, unreadOnly, page - 1, pageSize);
            int totalUnread = notificationDAO.countUnreadForUser(connection, internId);

            StringBuilder json = new StringBuilder("{\"items\":[");
            for (int index = 0; index < notifications.size(); index++) {
                if (index > 0) {
                    json.append(',');
                }
                appendNotification(json, notifications.get(index));
            }
            json.append("],\"totalUnread\":").append(totalUnread)
                    .append(",\"page\":").append(page)
                    .append(",\"pageSize\":").append(pageSize).append('}');
            response.getWriter().write(json.toString());
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải thông báo.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Integer internId = getAuthenticatedInternId(request);
        if (internId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");
        try (Connection connection = DBConnection.getConnection()) {
            switch (action == null ? "" : action) {
                case "mark-all-read" -> {
                    notificationService.markAllAsRead(connection, internId);
                    response.getWriter().write("{\"success\":true}");
                }
                case "mark-read" -> {
                    long notificationId = parseNotificationId(request.getParameter("id"));
                    if (!notificationService.markAsRead(connection, internId, notificationId)) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                    response.getWriter().write("{\"success\":true}");
                }
                case "click" -> {
                    long notificationId = parseNotificationId(request.getParameter("id"));
                    InternNotificationService.ClickResult result = notificationService.handleClickAndNavigate(
                            connection, internId, notificationId);
                    if (result.success()) {
                        response.getWriter().write("{\"isAvailable\":true,\"targetUrl\":"
                                + jsonString(result.targetUrl()) + "}");
                    } else {
                        response.getWriter().write("{\"isAvailable\":false,\"message\":"
                                + jsonString(result.errorMessage()) + "}");
                    }
                }
                default -> response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã thông báo không hợp lệ.");
        } catch (SQLException exception) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Integer getAuthenticatedInternId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !"INTERN".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            return null;
        }
        Object value = session.getAttribute("currentUser");
        if (!(value instanceof User)) {
            value = session.getAttribute("user");
        }
        if (value instanceof User user && user.getId() > 0) {
            return user.getId();
        }
        return null;
    }

    private int parseBoundedInt(String value, int defaultValue, int minimum, int maximum) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Math.max(minimum, Math.min(maximum, Integer.parseInt(value)));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private long parseNotificationId(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing notification id");
        }
        long id = Long.parseLong(value);
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid notification id");
        }
        return id;
    }

    private void appendNotification(StringBuilder json, Notification notification) {
        json.append("{\"id\":").append(notification.getId())
                .append(",\"type\":").append(jsonString(
                        notification.getType() == null ? null : notification.getType().name()))
                .append(",\"title\":").append(jsonString(notification.getTitle()))
                .append(",\"shortContent\":").append(jsonString(notification.getShortContent()))
                .append(",\"targetUrl\":").append(jsonString(notification.getTargetUrl()))
                .append(",\"targetEntityType\":").append(jsonString(notification.getTargetEntityType()))
                .append(",\"targetEntityId\":").append(notification.getTargetEntityId() == null
                        ? "null" : notification.getTargetEntityId())
                .append(",\"isRead\":").append(notification.isRead())
                .append(",\"createdAt\":").append(jsonString(notification.getCreatedAt() == null
                        ? null : notification.getCreatedAt().toString()))
                .append('}');
    }

    private String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder escaped = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.append('"').toString();
    }
}