package com.example.internmanagement.controller;

import com.example.internmanagement.dao.ReportDAO;
import com.example.internmanagement.model.ReportPayload;
import com.example.internmanagement.model.ReportSnapshot;
import com.example.internmanagement.model.User;
import com.example.internmanagement.util.ReportExporter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.Normalizer;
import java.sql.SQLException;
import java.util.Locale;

@WebServlet("/final-reports/download")
public class ReportDownloadServlet extends HttpServlet {
    private final ReportDAO reportDao = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!isHr(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        long id;
        try {
            id = Long.parseLong(req.getParameter("id"));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String format = clean(req.getParameter("format")).toLowerCase(Locale.ROOT);
        if (!"pdf".equals(format) && !"xlsx".equals(format)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Định dạng xuất không hợp lệ.");
            return;
        }

        try {
            ReportSnapshot snapshot = reportDao.findSnapshot(id);
            if (snapshot == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String school = clean(req.getParameter("school"));
            if (!snapshot.isLeaderReport() && school.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Vui lòng chọn trường cần xuất báo cáo.");
                return;
            }

            ReportPayload payload = ReportDAO.readPayload(snapshot);
            byte[] data = "pdf".equals(format)
                    ? ReportExporter.toPdf(payload, school)
                    : ReportExporter.toXlsx(payload, school);
            String scope = snapshot.isLeaderReport() ? "lanh-dao" : slug(school);
            String filename = "bao-cao-cuoi-ky-" + scope + "-" + snapshot.getId() + "." + format;

            resp.setContentType("pdf".equals(format)
                    ? "application/pdf"
                    : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            resp.setContentLength(data.length);
            resp.getOutputStream().write(data);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException | IllegalStateException e) {
            throw new ServletException("Không thể xuất báo cáo cuối kỳ.", e);
        }
    }

    private boolean isHr(HttpServletRequest req) {
        Object role = req.getSession().getAttribute("userRole");
        String value = role == null ? "" : role.toString().toUpperCase(Locale.ROOT);
        return "HR".equals(value) || "ADMIN".equals(value);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String slug(String value) {
        String plain = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return plain.isEmpty() ? "truong" : plain;
    }
}
