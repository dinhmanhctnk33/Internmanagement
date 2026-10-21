package com.example.internmanagement.controller;

import com.example.internmanagement.dao.RecipientDAO;
import com.example.internmanagement.dao.ReportDAO;
import com.example.internmanagement.dao.ReportSendDAO;
import com.example.internmanagement.model.RecipientContact;
import com.example.internmanagement.model.ReportPayload;
import com.example.internmanagement.model.ReportSendLog;
import com.example.internmanagement.model.ReportSnapshot;
import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import com.example.internmanagement.util.EmailUtility;
import com.example.internmanagement.util.ReportExporter;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.AddressException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Gửi báo cáo trực tiếp bằng email (luôn đính kèm PDF).
 *  GET  /final-reports/send?id=N                 : xem trước email + tệp đính kèm, chọn người nhận trong danh bạ
 *  POST /final-reports/send (action=send)        : xác nhận và gửi
 *  POST /final-reports/send (action=resend)      : gửi lại thủ công một địa chỉ "Gửi lỗi"
 */
@WebServlet("/final-reports/send")
public class ReportSendServlet extends HttpServlet {
    private static final int MAX_ATTACHMENT_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final int MAX_ATTEMPTS = 3;

    private final ReportDAO reportDao = new ReportDAO();
    private final RecipientDAO recipientDao = new RecipientDAO();
    private final ReportSendDAO sendDao = new ReportSendDAO();

    // ==================================================================
    // GET: màn hình xem trước
    // ==================================================================
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
        Long id = parseLong(req.getParameter("id"));
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            ReportSnapshot snapshot = reportDao.findSnapshot(id);
            if (snapshot == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            ReportPayload payload = ReportDAO.readPayload(snapshot);

            List<Map<String, Object>> groups = new ArrayList<>();
            if (snapshot.isLeaderReport()) {
                groups.add(group(snapshot, payload, "Ban lãnh đạo", null, recipientDao.leaders(), false));
            } else {
                for (ReportPayload.School s : payload.schools) {
                    List<RecipientContact> recipients = s.noSchoolInfo()
                            ? List.of() : recipientDao.forSchool(s.schoolName());
                    groups.add(group(snapshot, payload, s.schoolName(), s.schoolName(), recipients, s.noSchoolInfo()));
                }
            }

            req.setAttribute("snapshot", snapshot);
            req.setAttribute("groups", groups);
            req.setAttribute("emailSubject", subjectOf(snapshot));
            req.setAttribute("emailParagraphs", EmailUtility.reportEmailParagraphs(
                    "(họ tên người nhận)", payload.programNames, !snapshot.isLeaderReport()));
            req.setAttribute("smtpConfigured", EmailUtility.isConfigured());
            req.setAttribute("batchToken", UUID.randomUUID().toString()); // mỗi lần xác nhận một mã, chống gửi trùng
            req.setAttribute("currentUser", user);
            req.getRequestDispatcher("/WEB-INF/views/final-report-send.jsp").forward(req, resp);
        } catch (IllegalStateException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("Không thể tải màn hình gửi báo cáo.", e);
        }
    }

    // ==================================================================
    // POST: gửi / gửi lại
    // ==================================================================
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

        String action = clean(req.getParameter("action"));
        Long snapshotId = parseLong(req.getParameter("id"));
        String errorBase = "resend".equals(action) ? "/final-reports?id=" : "/final-reports/send?id=";
        try {
            if (snapshotId == null) throw new IllegalArgumentException("Bản chụp báo cáo không hợp lệ.");
            if (!EmailUtility.isConfigured())
                throw new IllegalArgumentException("Hệ thống chưa cấu hình email (SMTP) nên chưa gửi được.");
            ReportSnapshot snapshot = reportDao.findSnapshot(snapshotId);
            if (snapshot == null) throw new IllegalArgumentException("Không tìm thấy bản chụp báo cáo.");

            if ("send".equals(action)) {
                int[] result = send(req, user, snapshot);
                resp.sendRedirect(req.getContextPath() + "/final-reports?id=" + snapshotId
                        + "&sent=" + result[0] + "&failed=" + result[1]);
            } else if ("resend".equals(action)) {
                boolean ok = resend(req, user, snapshot);
                resp.sendRedirect(req.getContextPath() + "/final-reports?id=" + snapshotId
                        + (ok ? "&sent=1&failed=0" : "&sent=0&failed=1"));
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectError(req, resp, errorBase, snapshotId, e.getMessage());
        } catch (SQLException e) {
            redirectError(req, resp, errorBase, snapshotId, "Không thể ghi lịch sử gửi. Vui lòng thử lại.");
        }
    }

    /** Trả về {số gửi thành công, số gửi lỗi}. */
    private int[] send(HttpServletRequest req, User user, ReportSnapshot snapshot) throws SQLException {
        ReportPayload payload = ReportDAO.readPayload(snapshot);
        boolean leader = snapshot.isLeaderReport();

        // 1. Người nhận phải là người có thật trong danh bạ (không nhận địa chỉ gõ tay)
        List<Long> ids = new ArrayList<>(parseIds(req.getParameterValues("recipientIds")));
        if (ids.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất một người nhận trong danh bạ.");
        List<RecipientContact> chosen = recipientDao.findByIds(ids);
        if (chosen.size() < ids.size())
            throw new IllegalArgumentException("Có người nhận không còn trong danh bạ. Vui lòng tải lại trang.");

        // 2. Mỗi người nhận chỉ được nhận đúng loại báo cáo / đúng trường
        List<String[]> targets = new ArrayList<>();
        Map<String, RecipientContact> byEmail = new LinkedHashMap<>();
        for (RecipientContact c : chosen) {
            String school = null;
            if (leader) {
                if (!RecipientContact.TYPE_LEADER.equals(c.getRecipientType()))
                    throw new IllegalArgumentException("Báo cáo lãnh đạo chỉ gửi cho người nhận thuộc Ban lãnh đạo.");
            } else {
                if (!RecipientContact.TYPE_SCHOOL.equals(c.getRecipientType()))
                    throw new IllegalArgumentException("Báo cáo cho trường chỉ gửi cho người nhận thuộc trường.");
                for (ReportPayload.School s : payload.schools) {
                    if (!s.noSchoolInfo() && ReportDAO.normalizeSchool(s.schoolName())
                            .equals(ReportDAO.normalizeSchool(c.getOrganizationName()))) {
                        school = s.schoolName();
                    }
                }
                if (school == null)
                    throw new IllegalArgumentException("Người nhận " + c.getEmail()
                            + " không thuộc trường nào có trong báo cáo này.");
            }
            targets.add(new String[]{school, c.getEmail()});
            byEmail.putIfAbsent(c.getEmail().toLowerCase(Locale.ROOT), c);
        }

        // 3. Tạo PDF và kiểm tra dung lượng TRƯỚC khi gửi bất kỳ ai
        Map<String, byte[]> pdfs = new LinkedHashMap<>();
        for (String[] t : targets) {
            String key = t[0] == null ? "" : t[0];
            if (pdfs.containsKey(key)) continue;
            byte[] pdf = ReportExporter.toPdf(payload, t[0]);
            if (pdf.length > MAX_ATTACHMENT_BYTES)
                throw new IllegalArgumentException("Tệp PDF quá lớn để đính kèm email. Hãy xuất file và gửi "
                        + "ngoài hệ thống, hoặc tạo báo cáo nhỏ hơn theo từng chương trình.");
            pdfs.put(key, pdf);
        }

        // 4. Ghi PENDING cho từng địa chỉ. Mã xác nhận đã dùng rồi thì không tạo thêm dòng nào => không gửi trùng
        List<ReportSendLog> logs = sendDao.createBatch(snapshot.getId(), clean(req.getParameter("batchToken")),
                user.getId(), targets, req.getRemoteAddr());
        if (logs.isEmpty())
            throw new IllegalArgumentException("Lần gửi này đã được xử lý trước đó. Vui lòng xem lịch sử gửi "
                    + "trước khi gửi lại.");

        // 5. Gửi từng địa chỉ, lỗi một địa chỉ không ảnh hưởng địa chỉ khác
        int sent = 0, failed = 0;
        for (ReportSendLog log : logs) {
            RecipientContact contact = byEmail.get(log.getRecipientEmail().toLowerCase(Locale.ROOT));
            String key = log.getSchoolName() == null ? "" : log.getSchoolName();
            boolean ok = deliver(log, snapshot, payload, contact == null ? null : contact.getContactName(),
                    pdfs.get(key), 0);
            if (ok) sent++;
            else failed++;
        }
        return new int[]{sent, failed};
    }

    private boolean resend(HttpServletRequest req, User user, ReportSnapshot snapshot) throws SQLException {
        Long logId = parseLong(req.getParameter("logId"));
        ReportSendLog log = logId == null ? null : sendDao.find(logId);
        if (log == null || log.getSnapshotId() != snapshot.getId())
            throw new IllegalArgumentException("Không tìm thấy lần gửi cần gửi lại.");
        if (!ReportSendLog.FAILED.equals(log.getStatus()))
            throw new IllegalArgumentException("Chỉ gửi lại được các địa chỉ đang ở trạng thái \"Gửi lỗi\".");

        // Địa chỉ phải còn trong danh bạ (đúng trường nếu là báo cáo cho trường)
        List<RecipientContact> pool = snapshot.isLeaderReport()
                ? recipientDao.leaders() : recipientDao.forSchool(log.getSchoolName());
        RecipientContact contact = null;
        for (RecipientContact c : pool) {
            if (c.getEmail().equalsIgnoreCase(log.getRecipientEmail())) contact = c;
        }
        if (contact == null)
            throw new IllegalArgumentException("Địa chỉ này không còn trong danh bạ nên không thể gửi lại.");

        ReportPayload payload = ReportDAO.readPayload(snapshot);
        byte[] pdf = ReportExporter.toPdf(payload, log.getSchoolName());
        if (pdf.length > MAX_ATTACHMENT_BYTES)
            throw new IllegalArgumentException("Tệp PDF quá lớn để đính kèm email.");

        try (Connection c = DBConnection.getConnection()) {
            ReportDAO.audit(c, user.getId(), "RESEND_REPORT", "report_send_logs", log.getId(),
                    "{\"email\":\"" + log.getRecipientEmail().replace("\"", "") + "\"}", req.getRemoteAddr());
        }
        return deliver(log, snapshot, payload, contact.getContactName(), pdf, log.getAttempts());
    }

    /**
     * Gửi một email, thử lại tối đa MAX_ATTEMPTS lần nếu lỗi tạm thời, rồi ghi kết quả vào lịch sử.
     * Địa chỉ sai/không tồn tại thì không thử lại.
     */
    private boolean deliver(ReportSendLog log, ReportSnapshot snapshot, ReportPayload payload,
                            String contactName, byte[] pdf, int baseAttempts) throws SQLException {
        String subject = subjectOf(snapshot);
        List<String> paragraphs = EmailUtility.reportEmailParagraphs(contactName, payload.programNames,
                !snapshot.isLeaderReport());
        String fileName = fileName(snapshot, log.getSchoolName());

        int attempts = 0;
        String error = null;
        while (attempts < MAX_ATTEMPTS) {
            attempts++;
            try {
                EmailUtility.sendReportEmail(log.getRecipientEmail(), subject, paragraphs, pdf, fileName);
                sendDao.markSent(log.getId(), baseAttempts + attempts);
                return true;
            } catch (MessagingException | UnsupportedEncodingException e) {
                error = e.getMessage();
                boolean permanent = e instanceof AddressException || e instanceof SendFailedException;
                if (permanent || attempts >= MAX_ATTEMPTS) break;
                try {
                    Thread.sleep(attempts * 1000L); // chờ 1s, 2s rồi thử lại
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        sendDao.markFailed(log.getId(), baseAttempts + attempts, error);
        return false;
    }

    // ------------------------------------------------------------------
    private Map<String, Object> group(ReportSnapshot snapshot, ReportPayload payload, String label,
                                      String schoolName, List<RecipientContact> recipients, boolean noSchoolInfo) {
        String reason = null;
        int sizeKb = 0;
        if (noSchoolInfo) {
            reason = "Thực tập sinh chưa có thông tin trường: chỉ xuất file được, không gửi trực tiếp.";
        } else if (recipients.isEmpty()) {
            reason = "Chưa có người nhận trong danh bạ: chỉ xuất file được, không gửi trực tiếp.";
        } else {
            byte[] pdf = ReportExporter.toPdf(payload, schoolName);
            sizeKb = pdf.length / 1024;
            if (pdf.length > MAX_ATTACHMENT_BYTES)
                reason = "Tệp PDF quá lớn để đính kèm email (" + sizeKb + " KB). Hãy xuất file và gửi ngoài hệ thống, "
                        + "hoặc tạo báo cáo nhỏ hơn theo từng chương trình.";
        }
        Map<String, Object> g = new LinkedHashMap<>();
        g.put("label", label);
        g.put("schoolName", schoolName);
        g.put("recipients", recipients);
        g.put("canSend", reason == null);
        g.put("reason", reason);
        g.put("fileName", fileName(snapshot, schoolName));
        g.put("sizeKb", sizeKb);
        return g;
    }

    private String subjectOf(ReportSnapshot snapshot) {
        return snapshot.isLeaderReport() ? "Báo cáo tổng hợp thực tập cuối kỳ - IMS Portal"
                : "Báo cáo kết quả thực tập cuối kỳ - IMS Portal";
    }

    private String fileName(ReportSnapshot snapshot, String schoolName) {
        return snapshot.isLeaderReport() ? "bao-cao-lanh-dao-" + snapshot.getId() + ".pdf"
                : "bao-cao-" + slug(schoolName) + "-" + snapshot.getId() + ".pdf";
    }

    private String slug(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .replaceAll("[^A-Za-z0-9]+", "-").replaceAll("^-|-$", "");
        return n.isEmpty() ? "truong" : n.toLowerCase(Locale.ROOT);
    }

    private Set<Long> parseIds(String[] raw) {
        Set<Long> ids = new LinkedHashSet<>();
        if (raw != null) {
            for (String v : raw) {
                Long id = parseLong(v);
                if (id != null && id > 0) ids.add(id);
            }
        }
        return ids;
    }

    private void redirectError(HttpServletRequest req, HttpServletResponse resp, String base, Long id,
                               String message) throws IOException {
        String target = id == null ? "/final-reports?error=" : base + id + "&error=";
        resp.sendRedirect(req.getContextPath() + target + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    private boolean isHr(HttpServletRequest req) {
        Object role = req.getSession().getAttribute("userRole");
        String value = role == null ? "" : role.toString().toUpperCase(Locale.ROOT);
        return "HR".equals(value) || "ADMIN".equals(value);
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
