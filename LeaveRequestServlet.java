package com.example.internmanagement.controller;

import com.example.internmanagement.dao.LeaveRequestDAO;
import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.LocalDate;

/** INTERN: gửi/hủy đơn nghỉ. HR/ADMIN: duyệt/từ chối. Mỗi request là một transaction. */
@WebServlet("/leave-requests")
public class LeaveRequestServlet extends HttpServlet {
    private final LeaveRequestDAO dao = new LeaveRequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + ("INTERN".equals(role(req)) ? "/attendance" : "/attendance-report"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String role = role(req);
        boolean intern = "INTERN".equals(role);
        boolean hr = "HR".equals(role) || "ADMIN".equals(role);
        User user = (User) req.getSession().getAttribute("user");
        if (user == null || (!intern && !hr)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        String base = req.getContextPath() + (intern ? "/attendance" : "/attendance-report");
        String ip = req.getRemoteAddr();
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String result;
                if (intern && "cancel".equals(clean(req.getParameter("action")))) {
                    dao.cancel(c, user.getId(), id(req.getParameter("leaveId")), ip);
                    result = "cancelled";
                } else if (intern) {
                    dao.submit(c, user.getId(), clean(req.getParameter("leaveType")),
                            date(req.getParameter("startDate")), date(req.getParameter("endDate")),
                            req.getParameter("reason"), ip, LocalDate.now());
                    result = "created";
                } else {
                    dao.review(c, user.getId(), id(req.getParameter("leaveId")),
                            clean(req.getParameter("decision")), req.getParameter("note"), ip);
                    result = "reviewed";
                }
                c.commit();
                resp.sendRedirect(base + "?leave=" + result);
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(base + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            getServletContext().log("Lỗi xử lý đơn nghỉ phép", e);
            resp.sendRedirect(base + "?error=" + URLEncoder.encode(
                    "Không thể xử lý đơn nghỉ. Vui lòng thử lại.", StandardCharsets.UTF_8));
        }
    }

    private LocalDate date(String v) {
        try { return LocalDate.parse(clean(v)); }
        catch (Exception e) { throw new IllegalArgumentException("Vui lòng chọn ngày nghỉ hợp lệ."); }
    }
    private long id(String v) {
        try { long id = Long.parseLong(clean(v)); if (id > 0) return id; } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("Đơn nghỉ không hợp lệ.");
    }
    private String role(HttpServletRequest req) { return String.valueOf(req.getSession().getAttribute("userRole")).toUpperCase(); }
    private String clean(String v) { return v == null ? "" : v.trim(); }
}
