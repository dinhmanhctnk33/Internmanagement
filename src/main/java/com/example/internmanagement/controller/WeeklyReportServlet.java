package com.example.internmanagement.controller;

import com.example.internmanagement.dao.InternProfileDAO;
import com.example.internmanagement.exception.BusinessException;
import com.example.internmanagement.model.InternProfile;
import com.example.internmanagement.model.User;
import com.example.internmanagement.model.WeeklyReport;
import com.example.internmanagement.service.WeeklyReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/intern/weekly-report")
public class WeeklyReportServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(WeeklyReportServlet.class.getName());
    private final InternProfileDAO internProfileDAO = new InternProfileDAO();
    private WeeklyReportService reportService;

    @Override
    public void init() throws ServletException {
        Object configuredService = getServletContext().getAttribute("weeklyReportService");
        if (configuredService instanceof WeeklyReportService service) {
            reportService = service;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = currentUser(session);
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập.");
            return;
        }

        String role = session.getAttribute("userRole") instanceof String sessionRole
                ? sessionRole
                : user.getRoleCode();
        if (!"INTERN".equalsIgnoreCase(role)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
            return;
        }
        if (reportService == null) {
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Dịch vụ báo cáo tuần chưa được cấu hình.");
            return;
        }

        InternProfile intern;
        try {
            intern = internProfileDAO.findByUserId(user.getId());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Unable to resolve intern profile for weekly report", exception);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải hồ sơ thực tập sinh.");
            return;
        }
        if (intern == null) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản chưa có hồ sơ thực tập sinh.");
            return;
        }

        String internId = Integer.toString(intern.getId());
        String action = request.getParameter("action");
        try {
            if ("draft".equalsIgnoreCase(action)) {
                LocalDate weekStartDate = parseDate(request.getParameter("weekStartDate"));
                LocalDate weekEndDate = parseDate(request.getParameter("weekEndDate"));
                WeeklyReport report = reportService.saveDraft(
                        internId,
                        weekStartDate,
                        weekEndDate,
                        request.getParameter("tasksDone"),
                        request.getParameter("results"),
                        request.getParameter("issues"),
                        request.getParameter("nextWeekPlans"));
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\":true,\"message\":\"Lưu nháp thành công\",\"reportId\":"
                        + jsonString(report.getId()) + "}");
            } else if ("submit".equalsIgnoreCase(action)) {
                String reportId = request.getParameter("reportId");
                if (reportId == null || reportId.isBlank()) {
                    writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Mã báo cáo không hợp lệ.");
                    return;
                }

                WeeklyReport report = reportService.submitReport(
                        internId,
                        reportId,
                        request.getParameter("tasksDone"),
                        request.getParameter("results"),
                        request.getParameter("issues"),
                        request.getParameter("nextWeekPlans"));
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\":true,\"message\":\"Nộp báo cáo thành công\",\"isLate\":"
                        + report.isLate() + ",\"isResubmitted\":" + report.isResubmitted() + "}");
            } else {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Hành động không được hỗ trợ.");
            }
        } catch (BusinessException exception) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (DateTimeParseException exception) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Ngày báo cáo không hợp lệ.");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Unable to process weekly report request", exception);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Đã có lỗi hệ thống xảy ra. Vui lòng thử lại.");
        }
    }

    private User currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("currentUser");
        if (!(user instanceof User)) {
            user = session.getAttribute("user");
        }
        return user instanceof User currentUser ? currentUser : null;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new DateTimeParseException("Date is required", value == null ? "" : value, 0);
        }
        return LocalDate.parse(value.trim());
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"error\":" + jsonString(message) + "}");
    }

    private String jsonString(String value) {
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