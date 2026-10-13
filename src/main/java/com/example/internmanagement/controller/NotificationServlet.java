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

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final InternNotificationService notificationService = new InternNotificationService(notificationDAO);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            String openId = request.getParameter("open");
            if (openId != null && !openId.isBlank()) {
                try {
                    InternNotificationService.ClickResult result = notificationService.handleClickAndNavigate(
                            connection, currentUser.getId(), Long.parseLong(openId));
                    if (result.success()) {
                        response.sendRedirect(request.getContextPath() + result.targetUrl());
                        return;
                    }
                    int status = result.errorMessage().startsWith("Thông báo không tồn tại")
                            ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_GONE;
                    response.sendError(status, result.errorMessage());
                    return;
                } catch (NumberFormatException exception) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                            "Thông báo không tồn tại hoặc bạn không có quyền truy cập.");
                    return;
                }
            }

            int page = parsePositiveInt(request.getParameter("page"), 1);
            int pageSize = 20;
            boolean unreadOnly = "true".equalsIgnoreCase(request.getParameter("unreadOnly"));
            List<Notification> notifications = notificationService.getNotifications(
                    connection, currentUser.getId(), unreadOnly, page - 1, pageSize);
                notifications.forEach(item -> item.setTargetUrl(
                    "/notifications?open=" + item.getId()));
            request.setAttribute("notifications", notifications);
            request.setAttribute("recentNotifications", notifications.size() > 5 ? notifications.subList(0, 5) : notifications);
            request.setAttribute("unreadNotificationCount", notificationDAO.countUnreadForUser(connection, currentUser.getId()));
            request.setAttribute("unreadBadgeDisplay", notificationService.getUnreadBadgeDisplay(connection, currentUser.getId()));
            request.setAttribute("unreadOnly", unreadOnly);
            request.setAttribute("page", page);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("hasNextPage", notifications.size() == pageSize);
            request.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Không thể tải thông báo", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");
        try (Connection connection = DBConnection.getConnection()) {
            switch (action == null ? "" : action) {
                case "mark-read" -> notificationService.markAllAsRead(connection, currentUser.getId());
                case "mark-one" -> {
                    long notificationId = Long.parseLong(request.getParameter("notificationId"));
                    if (!notificationService.markAsRead(connection, currentUser.getId(), notificationId)) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                }
                default -> {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("currentUser");
        if (value instanceof User user) {
            return user;
        }
        value = session.getAttribute("user");
        return value instanceof User user ? user : null;
    }
}
