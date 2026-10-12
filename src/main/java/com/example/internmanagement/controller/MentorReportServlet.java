package com.example.internmanagement.controller;

import com.example.internmanagement.dao.MentorReportDAO;
import com.example.internmanagement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

@WebServlet({"/mentor/dashboard", "/mentor/reports"})
public class MentorReportServlet extends HttpServlet {
    private final MentorReportDAO dao = new MentorReportDAO();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentMentor(request, response); if (user == null) return;
        try {
            Long mentorId = dao.findMentorIdByUserId(user.getId());
            if (mentorId == null) { response.sendError(403, "Tài khoản chưa có hồ sơ mentor đang hoạt động."); return; }
            request.setAttribute("summary", dao.summary(mentorId));
            request.setAttribute("interns", dao.interns(mentorId));
            request.setAttribute("currentUser", user);
            request.setAttribute("recentNotifications", dao.recentNotifications(user.getId()));
            request.setAttribute("unreadNotificationCount", dao.unreadNotificationCount(user.getId()));
            if (request.getServletPath().endsWith("dashboard")) {
                request.setAttribute("recentReports", dao.reports(mentorId, null, "ALL").stream().limit(5).toList());
                request.getRequestDispatcher("/WEB-INF/views/mentor-dashboard.jsp").forward(request, response);
                return;
            }
            Long internId = positiveLong(request.getParameter("internId"));
            String status = validStatus(request.getParameter("status"));
            List<Map<String, Object>> reports = dao.reports(mentorId, internId, status);
            Long reportId = positiveLong(request.getParameter("reportId"));
            Map<String, Object> selected = reports.stream().filter(item -> reportId != null && reportId.equals(((Number)item.get("id")).longValue())).findFirst().orElse(reports.isEmpty() ? null : reports.get(0));
            request.setAttribute("reports", reports); request.setAttribute("selectedReport", selected);
            request.setAttribute("selectedInternId", internId); request.setAttribute("selectedStatus", status);
            request.getRequestDispatcher("/WEB-INF/views/mentor-reports.jsp").forward(request, response);
        } catch (SQLException exception) { throw new ServletException("Không thể tải dữ liệu báo cáo mentor.", exception); }
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        User user = currentMentor(request, response); if (user == null) return;
        try {
            Long mentorId = dao.findMentorIdByUserId(user.getId());
            if (mentorId == null) throw new IllegalArgumentException("Tài khoản mentor không hợp lệ.");
            long reportId = requiredId(request.getParameter("reportId"));
            String decision = "reject".equals(request.getParameter("action")) ? "REJECTED" : "APPROVED";
            String feedback = Optional.ofNullable(request.getParameter("feedback")).orElse("").trim();
            if (feedback.length() < 5) throw new IllegalArgumentException("Phản hồi cần có ít nhất 5 ký tự.");
            Double score = parseScore(request.getParameter("score"));
            if ("APPROVED".equals(decision) && score == null) throw new IllegalArgumentException("Vui lòng nhập điểm khi duyệt báo cáo.");
            dao.review(mentorId, user.getId(), reportId, decision, score, feedback, request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/mentor/reports?reportId=" + reportId + "&success=reviewed");
        } catch (SQLException | IllegalArgumentException exception) {
            response.sendRedirect(request.getContextPath() + "/mentor/reports?error=" + URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private User currentMentor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object value = request.getSession().getAttribute("user");
        if (!(value instanceof User user) || !"MENTOR".equalsIgnoreCase(user.getRole())) { response.sendError(403); return null; }
        return user;
    }
    private Long positiveLong(String value) { try { long id=Long.parseLong(value); return id>0?id:null; } catch(Exception ignored){return null;} }
    private long requiredId(String value) { Long id=positiveLong(value); if(id==null) throw new IllegalArgumentException("Báo cáo không hợp lệ."); return id; }
    private String validStatus(String value) { return value != null && Set.of("SUBMITTED","APPROVED","REJECTED").contains(value) ? value : "ALL"; }
    private Double parseScore(String value) { if(value==null||value.isBlank()) return null; try { double score=Double.parseDouble(value); if(score<0||score>10) throw new Exception(); return score; } catch(Exception e){throw new IllegalArgumentException("Điểm phải nằm trong khoảng 0 đến 10.");} }
}
