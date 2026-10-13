package com.example.internmanagement.controller;

import com.example.internmanagement.dao.ContractActivityLogDAO;
import com.example.internmanagement.dao.ContractDAO;
import com.example.internmanagement.model.Contract;
import com.example.internmanagement.model.ContractActivityLog;
import com.example.internmanagement.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Luồng nghiệp vụ phía Thực tập sinh:
 *   GET  /api/my-contract                     -> xem hợp đồng của chính mình
 *   POST /api/my-contract/confirm              -> xác nhận (idempotent)
 *   POST /api/my-contract/request-amendment    -> gửi yêu cầu chỉnh kèm lý do
 *
 * GHI CHÚ VỀ ĐĂNG NHẬP: project đang dùng cơ chế xác thực nào (session,
 * token...) mình chưa có thông tin, nên tạm lấy internProfileId/userId từ
 * HttpSession (currentInternProfileId/currentUserId bên dưới). Nếu nhóm
 * đã có class/filter xác thực riêng, thay 2 hàm đó bằng cách lấy đúng
 * theo cơ chế thật của project.
 *
 * Quy tắc bắt buộc theo tài liệu:
 *   - Thực tập sinh chỉ thấy hợp đồng của chính mình.
 *   - Nút "Xác nhận" chỉ dùng được sau khi đã mở xem.
 *   - Xác nhận / yêu cầu chỉnh chạy lặp KHÔNG tạo kết quả trùng (xem ContractDAO).
 */
@WebServlet(name = "InternContractController", urlPatterns = {"/api/my-contract", "/api/my-contract/*"})
public class InternContractController extends HttpServlet {

    private final ContractDAO contractDAO = new ContractDAO();
    private final ContractActivityLogDAO activityLogDAO = new ContractActivityLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long internProfileId = currentInternProfileId(req);
        resp.setContentType("application/json;charset=UTF-8");

        Contract contract = contractDAO.findActiveByInternProfileId(internProfileId);
        try (PrintWriter out = resp.getWriter()) {
            if (contract == null) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "NO_CONTRACT");
                result.put("message", "Chưa có hợp đồng");
                out.print(JsonUtil.toJson(result));
                return;
            }

            contractDAO.markViewedIfFirstTime(contract.getId(), contract.getFileVersion());
            Contract refreshed = contractDAO.findById(contract.getId());
            out.print(JsonUtil.toJson(toMap(refreshed)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String path = req.getPathInfo();
        if (path == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (path.equals("/confirm")) {
            handleConfirm(req, resp);
        } else if (path.equals("/request-amendment")) {
            handleRequestAmendment(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleConfirm(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long internProfileId = currentInternProfileId(req);
        Long internUserId = currentUserId(req);

        Map<String, String> body = parseJsonBody(req);
        Long contractId = toLong(body.get("contractId"));

        Contract contract = contractDAO.findByIdForIntern(contractId, internProfileId);
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            if (contract == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.toJson(simpleMessage("Không tìm thấy hợp đồng")));
                return;
            }
            if (contract.getViewedAt() == null) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(JsonUtil.toJson(simpleMessage("Vui lòng mở xem hợp đồng trước khi xác nhận")));
                return;
            }

            boolean firstTimeConfirmed = contractDAO.confirm(
                    contract.getId(), contract.getFileVersion(), internUserId, "INTERN");

            if (firstTimeConfirmed) {
                activityLogDAO.insert(buildLog(contract.getId(), "CONFIRMED", internUserId, "INTERN",
                        contract.getFileVersion(), null, null, null));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "CONFIRMED");
                result.put("message", "Đã hoàn tất thủ tục xác nhận hợp đồng");
                out.print(JsonUtil.toJson(result));
                return;
            }

            Contract latest = contractDAO.findById(contract.getId());
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            String message = "AMENDMENT_REQUESTED".equals(latest.getStatus())
                    ? "Hợp đồng đang ở trạng thái yêu cầu chỉnh, không thể xác nhận"
                    : (latest.getFileVersion().equals(contract.getFileVersion())
                        ? "Hợp đồng đã được xác nhận trước đó"
                        : "Hợp đồng đã thay đổi, vui lòng tải lại và xem bản mới");
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", latest.getStatus());
            result.put("message", message);
            out.print(JsonUtil.toJson(result));
        }
    }

    private void handleRequestAmendment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long internProfileId = currentInternProfileId(req);
        Long internUserId = currentUserId(req);

        Map<String, String> body = parseJsonBody(req);
        Long contractId = toLong(body.get("contractId"));
        String reason = body.get("reason") == null ? "" : body.get("reason").trim();

        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            if (reason.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("field", "reason");
                result.put("message", "Lý do yêu cầu chỉnh là bắt buộc");
                out.print(JsonUtil.toJson(result));
                return;
            }

            Contract contract = contractDAO.findByIdForIntern(contractId, internProfileId);
            if (contract == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.toJson(simpleMessage("Không tìm thấy hợp đồng")));
                return;
            }

            boolean firstTimeRequested = contractDAO.requestAmendment(
                    contract.getId(), contract.getFileVersion(), reason);

            if (firstTimeRequested) {
                activityLogDAO.insert(buildLog(contract.getId(), "AMENDMENT_REQUESTED", internUserId, "INTERN",
                        contract.getFileVersion(), reason, null, null));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", "AMENDMENT_REQUESTED");
                result.put("message", "Đang chờ HR xử lý");
                out.print(JsonUtil.toJson(result));
                return;
            }

            Contract latest = contractDAO.findById(contract.getId());
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            String message = "AMENDMENT_REQUESTED".equals(latest.getStatus())
                    ? "Đang chờ HR xử lý"
                    : "Hợp đồng đã thay đổi, vui lòng tải lại và xem bản mới";
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", latest.getStatus());
            result.put("message", message);
            out.print(JsonUtil.toJson(result));
        }
    }

    private Map<String, Object> toMap(Contract c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("contractNumber", c.getContractNumber());
        m.put("status", c.getStatus());
        m.put("contractPdfUrl", c.getContractPdfUrl());
        m.put("fileVersion", c.getFileVersion());
        m.put("viewedAt", c.getViewedAt() == null ? null : c.getViewedAt().toString());
        m.put("confirmedAt", c.getConfirmedAt() == null ? null : c.getConfirmedAt().toString());
        m.put("amendmentReason", c.getAmendmentReason());
        return m;
    }

    private Map<String, Object> simpleMessage(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("message", message);
        return m;
    }

    private ContractActivityLog buildLog(Long contractId, String action, Long actorUserId, String source,
                                          Integer fileVersion, String reason, String oldValue, String newValue) {
        ContractActivityLog log = new ContractActivityLog();
        log.setContractId(contractId);
        log.setActionType(action);
        log.setActorUserId(actorUserId);
        log.setActorSource(source);
        log.setFileVersion(fileVersion);
        log.setReason(reason);
        log.setOldValueJson(oldValue);
        log.setNewValueJson(newValue);
        return log;
    }

    /** TODO: thay bằng cách lấy thông tin thực từ cơ chế đăng nhập thật của project. */
    private Long currentInternProfileId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object value = session != null ? session.getAttribute("internProfileId") : null;
        if (value == null) throw new IllegalStateException("Chưa đăng nhập hoặc thiếu internProfileId trong session");
        return Long.valueOf(value.toString());
    }

    private Long currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object value = session != null ? session.getAttribute("userId") : null;
        if (value == null) throw new IllegalStateException("Chưa đăng nhập hoặc thiếu userId trong session");
        return Long.valueOf(value.toString());
    }

    private Map<String, String> parseJsonBody(HttpServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            String body = reader.lines().collect(Collectors.joining());
            return JsonUtil.parseFlatObject(body);
        }
    }

    private Long toLong(String value) {
        if (value == null || value.isBlank()) return null;
        return Long.valueOf(value.trim());
    }
}
