package com.example.internmanagement.controller;

import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.dao.RecipientDAO;
import com.example.internmanagement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

/** Màn hình "Danh bạ người nhận": HR thêm, sửa, xóa, tìm kiếm người nhận báo cáo. */
@WebServlet("/report-recipients")
public class ReportRecipientServlet extends HttpServlet {
    private final RecipientDAO dao = new RecipientDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!isHr(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String type = clean(req.getParameter("type"));
        if (!Set.of("SCHOOL", "LEADER").contains(type)) type = "";
        String keyword = clean(req.getParameter("q"));

        try {
            req.setAttribute("recipients", dao.list(type, keyword));
            req.setAttribute("schoolNames", dao.schoolNames());
            Long edit = parseLong(req.getParameter("edit"));
            if (edit != null) req.setAttribute("editing", dao.find(edit));
            req.setAttribute("selectedType", type);
            req.setAttribute("keyword", keyword);
            req.setAttribute("currentUser", user);
            NotificationDAO notifications = new NotificationDAO();
            req.setAttribute("recentNotifications", notifications.findRecent(user.getId(), 5));
            req.setAttribute("unreadNotificationCount", notifications.unreadCount(user.getId()));
            req.getRequestDispatcher("/WEB-INF/views/report-recipients.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Không thể tải danh bạ người nhận.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!isHr(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String ip = req.getRemoteAddr();
        try {
            String action = clean(req.getParameter("action"));
            switch (action) {
                case "create" -> {
                    dao.create(clean(req.getParameter("recipientType")), clean(req.getParameter("organizationName")),
                            clean(req.getParameter("contactName")), clean(req.getParameter("email")),
                            user.getId(), ip);
                    redirect(req, resp, "success", "created");
                }
                case "update" -> {
                    dao.update(requiredId(req.getParameter("id")), clean(req.getParameter("recipientType")),
                            clean(req.getParameter("organizationName")), clean(req.getParameter("contactName")),
                            clean(req.getParameter("email")), user.getId(), ip);
                    redirect(req, resp, "success", "updated");
                }
                case "delete" -> {
                    dao.delete(requiredId(req.getParameter("id")), user.getId(), ip);
                    redirect(req, resp, "success", "deleted");
                }
                default -> throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException e) {
            redirect(req, resp, "error", e.getMessage());
        } catch (SQLException e) {
            // RecipientDAO bọc lỗi trùng email thành SQLException có cause là IllegalArgumentException
            String message = e.getCause() instanceof IllegalArgumentException
                    ? e.getCause().getMessage()
                    : "Không thể lưu danh bạ. Vui lòng thử lại.";
            redirect(req, resp, "error", message);
        }
    }

    // ------------------------------------------------------------------
    private void redirect(HttpServletRequest req, HttpServletResponse resp, String key, String value)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + "/report-recipients?" + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private boolean isHr(HttpServletRequest req) {
        Object role = req.getSession().getAttribute("userRole");
        String value = role == null ? "" : role.toString().toUpperCase(Locale.ROOT);
        return "HR".equals(value) || "ADMIN".equals(value);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private long requiredId(String value) {
        Long id = parseLong(value);
        if (id == null) throw new IllegalArgumentException("Người nhận không hợp lệ.");
        return id;
    }
}
