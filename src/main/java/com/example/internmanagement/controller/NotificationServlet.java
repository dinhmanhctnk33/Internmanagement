package com.example.internmanagement.controller;

import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
    private final NotificationDAO dao = new NotificationDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
        User user = user(req, resp); if (user == null) return;
        try {
            req.setAttribute("currentUser", user);
            req.setAttribute("notifications", dao.findRecent(user.getId(),100));
            req.setAttribute("recentNotifications", dao.findRecent(user.getId(),5));
            req.setAttribute("unreadNotificationCount", dao.unreadCount(user.getId()));
            req.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(req,resp);
        } catch(SQLException e) { throw new ServletException("Không thể tải thông báo.",e); }
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException {
        User user = user(req, resp); if (user == null) return;
        try {
            String id = req.getParameter("id");
            if (id == null || id.isBlank()) dao.markAllRead(user.getId());
            else dao.markRead(user.getId(), Long.parseLong(id));
        } catch(SQLException|NumberFormatException ignored) { }
        String target = req.getParameter("target");
        if (target == null || !target.startsWith("/") || target.startsWith("//")) target = "/notifications";
        resp.sendRedirect(req.getContextPath()+target);
    }

    private User user(HttpServletRequest req,HttpServletResponse resp)throws IOException {
        Object value=req.getSession().getAttribute("user");
        if(value instanceof User user)return user;
        resp.sendRedirect(req.getContextPath()+"/login");return null;
    }
}
