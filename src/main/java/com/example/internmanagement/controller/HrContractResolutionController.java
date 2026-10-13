package com.example.internmanagement.controller;

import com.example.internmanagement.dao.ContractActivityLogDAO;
import com.example.internmanagement.dao.ContractDAO;
import com.example.internmanagement.model.Contract;
import com.example.internmanagement.model.ContractActivityLog;
import com.example.internmanagement.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Luồng nghiệp vụ phía HR để xử lý hợp đồng đang "Yêu cầu chỉnh", hoặc
 * thao tác thủ công trên hợp đồng ký giấy.
 *
 * POST /api/hr/contracts/{id}/resolve
 * Body: { "action": "REUPLOAD" | "EDIT_DATES" | "CLOSE_REQUEST" | "MANUAL_CONFIRM", ... }
 */
@WebServlet(name = "HrContractResolutionController", urlPatterns = {"/api/hr/contracts/*"})
public class HrContractResolutionController extends HttpServlet {

    private final ContractDAO contractDAO = new ContractDAO();
    private final ContractActivityLogDAO activityLogDAO = new ContractActivityLogDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo(); // "/{id}/resolve"
        resp.setContentType("application/json;charset=UTF-8");

        if (pathInfo == null || !pathInfo.endsWith("/resolve")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Long contractId = extractContractId(pathInfo);
        Long hrUserId = currentUserId(req);
        Map<String, String> body = parseJsonBody(req);
        String action = body.get("action");

        Contract contract = contractDAO.findById(contractId);
        try (PrintWriter out = resp.getWriter()) {
            if (contract == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.toJson(simpleMessage("Không tìm thấy hợp đồng")));
                return;
            }

            if (action == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.toJson(simpleMessage("Thiếu action")));
                return;
            }

            switch (action) {
                case "REUPLOAD":
                    handleReupload(contract, body, hrUserId, out);
                    break;
                case "EDIT_DATES":
                    handleEditDates(contract, body, hrUserId, out);
                    break;
                case "CLOSE_REQUEST":
                    handleCloseRequest(contract, hrUserId, resp, out);
                    break;
                case "MANUAL_CONFIRM":
                    handleManualConfirm(contract, hrUserId, resp, out);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(JsonUtil.toJson(simpleMessage("action không hợp lệ")));
            }
        }
    }

    private void handleReupload(Contract contract, Map<String, String> body, Long hrUserId, PrintWriter out) {
        String newPdfUrl = body.get("contractPdfUrl");
        contractDAO.reuploadFile(contract.getId(), newPdfUrl);
        activityLogDAO.insert(buildLog(contract.getId(), "HR_REUPLOADED", hrUserId,
                contract.getFileVersion() + 1, null, null, null));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UNCONFIRMED");
        result.put("message", "Đã tải lên hợp đồng mới, thực tập sinh cần xác nhận lại");
        out.print(JsonUtil.toJson(result));
    }

    private void handleEditDates(Contract contract, Map<String, String> body, Long hrUserId, PrintWriter out) {
        Date newEffective = Date.valueOf(body.get("effectiveDate"));
        Date newExpiration = Date.valueOf(body.get("expirationDate"));
        boolean wasConfirmed = "CONFIRMED".equals(contract.getStatus());

        String oldValueJson = "{\"effectiveDate\":\"" + contract.getEffectiveDate()
                + "\",\"expirationDate\":\"" + contract.getExpirationDate() + "\"}";
        String newValueJson = "{\"effectiveDate\":\"" + newEffective
                + "\",\"expirationDate\":\"" + newExpiration + "\"}";

        contractDAO.editDates(contract.getId(), newEffective, newExpiration);
        activityLogDAO.insert(buildLog(contract.getId(), "DATES_EDITED", hrUserId,
                contract.getFileVersion(), null, oldValueJson, newValueJson));

        Map<String, Object> result = new LinkedHashMap<>();
        if (wasConfirmed) {
            result.put("status", "CONFIRMED");
            result.put("warning", "Hợp đồng này đã được thực tập sinh xác nhận trước đó. " +
                    "Việc sửa ngày không làm mất xác nhận, nhưng đã được ghi vào nhật ký.");
        } else {
            result.put("status", "UNCONFIRMED");
            result.put("message", "Đã cập nhật ngày, thực tập sinh cần xác nhận lại");
        }
        out.print(JsonUtil.toJson(result));
    }

    private void handleCloseRequest(Contract contract, Long hrUserId, HttpServletResponse resp, PrintWriter out) {
        boolean closed = contractDAO.closeAmendmentRequest(contract.getId());
        if (!closed) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(JsonUtil.toJson(simpleMessage("Hợp đồng không ở trạng thái Yêu cầu chỉnh")));
            return;
        }
        activityLogDAO.insert(buildLog(contract.getId(), "AMENDMENT_CLOSED", hrUserId,
                contract.getFileVersion(), null, null, null));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UNCONFIRMED");
        result.put("message", "Đã đóng yêu cầu chỉnh, thực tập sinh có thể xác nhận lại");
        out.print(JsonUtil.toJson(result));
    }

    private void handleManualConfirm(Contract contract, Long hrUserId, HttpServletResponse resp, PrintWriter out) {
        boolean confirmedNow = contractDAO.confirm(contract.getId(), contract.getFileVersion(), hrUserId, "HR");
        if (!confirmedNow) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(JsonUtil.toJson(simpleMessage("Hợp đồng không ở trạng thái có thể xác nhận thủ công")));
            return;
        }
        activityLogDAO.insert(buildLog(contract.getId(), "HR_MANUAL_CONFIRMED", hrUserId,
                contract.getFileVersion(), null, null, null));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "CONFIRMED");
        result.put("message", "Đã đặt trạng thái Đã xác nhận thủ công (nguồn: HR)");
        out.print(JsonUtil.toJson(result));
    }

    private Map<String, Object> simpleMessage(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("message", message);
        return m;
    }

    private ContractActivityLog buildLog(Long contractId, String action, Long actorUserId,
                                          Integer fileVersion, String reason, String oldValue, String newValue) {
        ContractActivityLog log = new ContractActivityLog();
        log.setContractId(contractId);
        log.setActionType(action);
        log.setActorUserId(actorUserId);
        log.setActorSource("HR");
        log.setFileVersion(fileVersion);
        log.setReason(reason);
        log.setOldValueJson(oldValue);
        log.setNewValueJson(newValue);
        return log;
    }

    private Long extractContractId(String pathInfo) {
        // pathInfo dạng "/12/resolve"
        String[] parts = pathInfo.split("/");
        return Long.valueOf(parts[1]);
    }

    /** TODO: thay bằng cách lấy userId thật từ cơ chế đăng nhập của HR trong project. */
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
}
