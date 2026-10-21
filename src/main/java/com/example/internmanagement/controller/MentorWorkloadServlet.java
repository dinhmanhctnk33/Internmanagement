package com.example.internmanagement.controller;

import com.example.internmanagement.dao.MentorManagementDAO;
import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/mentor-workload")
public class MentorWorkloadServlet extends HttpServlet {
    private static final int RECOMMENDED_CAPACITY = 5;
    private final MentorManagementDAO mentorDAO = new MentorManagementDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String keyword = clean(request.getParameter("q"));
        try {
            List<Map<String, Object>> mentors = mentorDAO.findAll(keyword, null, "ACTIVE");
            int interns = mentors.stream().mapToInt(row -> ((Number) row.get("intern_count")).intValue()).sum();
            long overloaded = mentors.stream().filter(row -> ((Number) row.get("intern_count")).intValue() > RECOMMENDED_CAPACITY).count();
            request.setAttribute("mentors", mentors);
            request.setAttribute("keyword", keyword);
            request.setAttribute("mentorCount", mentors.size());
            request.setAttribute("internCount", interns);
            request.setAttribute("overloadedCount", overloaded);
            request.setAttribute("capacity", RECOMMENDED_CAPACITY);
            request.setAttribute("currentUser", user);
            request.setAttribute("recentNotifications", notificationDAO.findRecent(user.getId(), 5));
            request.setAttribute("unreadNotificationCount", notificationDAO.unreadCount(user.getId()));
            request.getRequestDispatcher("/WEB-INF/views/mentor-workload.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải khối lượng công việc mentor.", exception);
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
