package com.example.internmanagement.controller;

import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.dao.ReportDAO;
import com.example.internmanagement.dao.ReportSendDAO;
import com.example.internmanagement.model.ReportSnapshot;
import com.example.internmanagement.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Màn hình "Báo cáo cuối kỳ" của HR:
 *  - GET  /final-reports                          : form tạo + lịch sử các bản chụp
 *  - GET  /final-reports?action=preview&...       : xem trước (chưa lưu gì)
 *  - GET  /final-reports?id=N                     : chi tiết một bản chụp + lịch sử gửi
 *  - POST /final-reports (action=create)          : xác nhận, lưu bản chụp bất biến
 */
@WebServlet("/final-reports")
public class FinalReportServlet extends HttpServlet {
    // Đọc JSON thành Map/List để JSP (EL) hiển thị được; số nguyên giữ là Long, số thập phân là Double
    private static final Gson MAP_GSON = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();

    private final ReportDAO reportDao = new ReportDAO();
    private final ReportSendDAO sendDao = new ReportSendDAO();

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

        try {
            String idParam = clean(req.getParameter("id"));
            if (!idParam.isEmpty()) {
                showDetail(req, resp, user, idParam);
            } else {
                showMain(req, resp, user);
            }
        } catch (SQLException e) {
            throw new ServletException("Không thể tải báo cáo cuối kỳ.", e);
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

        try {
            if (!"create".equals(clean(req.getParameter("action"))))
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            if (!"true".equals(clean(req.getParameter("confirmed"))))
                throw new IllegalArgumentException("Vui lòng xem trước và xác nhận trước khi tạo báo cáo.");

            List<String> types = typesOf(clean(req.getParameter("reportType")));
            List<Long> programIds = new ArrayList<>(parseIds(req));
            if (programIds.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất một chương trình.");

            List<Long> created = new ArrayList<>();
            for (String type : types) {
                created.add(reportDao.saveSnapshot(type, programIds, user.getId(), user.getFullName(),
                        req.getRemoteAddr()));
            }
            // Tạo một bản chụp thì mở luôn chi tiết; tạo cả hai loại thì về danh sách
            String target = created.size() == 1
                    ? "/final-reports?id=" + created.get(0) + "&success=created"
                    : "/final-reports?success=created";
            resp.sendRedirect(req.getContextPath() + target);
        } catch (IllegalArgumentException e) {
            redirectError(req, resp, e.getMessage());
        } catch (SQLException e) {
            // Lỗi giữa chừng: transaction đã rollback nên không có bản chụp dở dang, HR thử lại được
            redirectError(req, resp, "Không thể tạo báo cáo. Vui lòng thử lại.");
        }
    }

    // ------------------------------------------------------------------
    private void showMain(HttpServletRequest req, HttpServletResponse resp, User user)
            throws SQLException, ServletException, IOException {
        String reportType = clean(req.getParameter("reportType"));
        Set<Long> programIds = parseIds(req);
        req.setAttribute("selectedType", reportType);
        req.setAttribute("selectedProgramIds", programIds);

        if ("preview".equals(clean(req.getParameter("action")))) {
            try {
                List<Map<String, Object>> previews = new ArrayList<>();
                for (String type : typesOf(reportType)) {
                    previews.add(reportDao.preview(type, new ArrayList<>(programIds)));
                }
                req.setAttribute("previews", previews);
            } catch (IllegalArgumentException e) {
                req.setAttribute("errorMessage", e.getMessage());
            }
        }

        req.setAttribute("programs", reportDao.programs());
        req.setAttribute("snapshots", reportDao.listSnapshots());
        req.setAttribute("sendSummaries", sendDao.summaries());
        addHeaderData(req, user);
        req.getRequestDispatcher("/WEB-INF/views/final-reports.jsp").forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp, User user, String idParam)
            throws SQLException, ServletException, IOException {
        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ReportSnapshot snapshot = reportDao.findSnapshot(id);
        if (snapshot == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Map<String, Object> payload = MAP_GSON.fromJson(snapshot.getPayloadJson(),
                new TypeToken<Map<String, Object>>() {}.getType());

        req.setAttribute("snapshot", snapshot);
        req.setAttribute("payload", payload);
        req.setAttribute("sendLogs", sendDao.listBySnapshot(id));
        addHeaderData(req, user);
        req.getRequestDispatcher("/WEB-INF/views/final-report-detail.jsp").forward(req, resp);
    }

    // ------------------------------------------------------------------
    private void addHeaderData(HttpServletRequest req, User user) throws SQLException {
        NotificationDAO notifications = new NotificationDAO();
        req.setAttribute("currentUser", user);
        req.setAttribute("recentNotifications", notifications.findRecent(user.getId(), 5));
        req.setAttribute("unreadNotificationCount", notifications.unreadCount(user.getId()));
    }

    private List<String> typesOf(String reportType) {
        return switch (reportType) {
            case "LEADER" -> List.of("LEADER");
            case "SCHOOL" -> List.of("SCHOOL");
            case "BOTH" -> List.of("LEADER", "SCHOOL");
            default -> throw new IllegalArgumentException("Vui lòng chọn loại báo cáo.");
        };
    }

    private Set<Long> parseIds(HttpServletRequest req) {
        Set<Long> ids = new LinkedHashSet<>();
        String[] raw = req.getParameterValues("programIds");
        if (raw != null) {
            for (String value : raw) {
                try {
                    long id = Long.parseLong(value.trim());
                    if (id > 0) ids.add(id);
                } catch (NumberFormatException ignored) {
                    // bỏ qua giá trị không hợp lệ
                }
            }
        }
        return ids;
    }

    private void redirectError(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/final-reports?error="
                + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    private boolean isHr(HttpServletRequest req) {
        Object role = req.getSession().getAttribute("userRole");
        String value = role == null ? "" : role.toString().toUpperCase(Locale.ROOT);
        return "HR".equals(value) || "ADMIN".equals(value);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
