package com.example.internmanagement.controller;

import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.dao.WorkScheduleDAO;
import com.example.internmanagement.model.User;
import com.example.internmanagement.model.WorkSchedule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@WebServlet("/work-schedules")
public class WorkScheduleServlet extends HttpServlet {
    private final WorkScheduleDAO dao = new WorkScheduleDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            req.setAttribute("schedules", dao.findAll());
            req.setAttribute("groups", dao.activeGroups());
            Long editId = positiveLong(req.getParameter("edit"));
            if (editId != null) req.setAttribute("editing", dao.find(editId));
            NotificationDAO notifications = new NotificationDAO();
            req.setAttribute("currentUser", user);
            req.setAttribute("recentNotifications", notifications.findRecent(user.getId(), 5));
            req.setAttribute("unreadNotificationCount", notifications.unreadCount(user.getId()));
            req.getRequestDispatcher("/WEB-INF/views/work-schedules.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Không thể tải lịch làm việc.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        if (req.getSession().getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            String action = clean(req.getParameter("action"));
            if ("delete".equals(action)) {
                Long id = positiveLong(req.getParameter("id"));
                if (id == null || !dao.delete(id)) throw new IllegalArgumentException("Lịch làm việc không tồn tại.");
                redirect(req, resp, "deleted");
                return;
            }
            long id = "update".equals(action) ? requiredLong(req.getParameter("id"), "Lịch làm việc không hợp lệ.") : 0;
            long groupId = requiredLong(req.getParameter("groupId"), "Vui lòng chọn nhóm.");
            String shiftName = clean(req.getParameter("shiftName"));
            String start = clean(req.getParameter("startTime"));
            String end = clean(req.getParameter("endTime"));
            String note = clean(req.getParameter("note"));
            if (shiftName.isEmpty() || shiftName.length() > 100) throw new IllegalArgumentException("Tên lịch là bắt buộc và không quá 100 ký tự.");
            if (note.length() > 1000) throw new IllegalArgumentException("Ghi chú không quá 1000 ký tự.");
            LocalTime startTime = parseTime(start);
            LocalTime endTime = parseTime(end);
            if (!endTime.isAfter(startTime)) throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu.");
            String workDays = days(req.getParameterValues("workDays"));
            dao.save(new WorkSchedule(id, groupId, null, shiftName, start, end, workDays, note));
            redirect(req, resp, id > 0 ? "updated" : "created");
        } catch (IllegalArgumentException e) {
            redirectError(req, resp, e.getMessage());
        } catch (SQLException e) {
            redirectError(req, resp, e.getErrorCode() == 1062 ? "Nhóm này đã có lịch làm việc." : "Không thể lưu lịch làm việc.");
        }
    }

    private String days(String[] values) {
        if (values == null || values.length == 0) throw new IllegalArgumentException("Vui lòng chọn ít nhất một ngày làm việc.");
        Set<String> allowed = Set.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
        Set<String> result = new LinkedHashSet<>();
        for (String value : values) if (allowed.contains(value)) result.add(value);
        if (result.isEmpty()) throw new IllegalArgumentException("Ngày làm việc không hợp lệ.");
        return String.join(",", result);
    }

    private LocalTime parseTime(String value) {
        try { return LocalTime.parse(value); }
        catch (Exception e) { throw new IllegalArgumentException("Giờ làm việc không hợp lệ."); }
    }

    private Long positiveLong(String value) {
        try { long id = Long.parseLong(value); return id > 0 ? id : null; }
        catch (Exception e) { return null; }
    }

    private long requiredLong(String value, String message) {
        Long id = positiveLong(value);
        if (id == null) throw new IllegalArgumentException(message);
        return id;
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
    private void redirect(HttpServletRequest req, HttpServletResponse resp, String value) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/work-schedules?success=" + value);
    }
    private void redirectError(HttpServletRequest req, HttpServletResponse resp, String value) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/work-schedules?error=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
