package com.example.internmanagement.controller;

import com.example.internmanagement.dao.MentorFinalEvaluationDAO;
import com.example.internmanagement.dao.MentorReportDAO;
import com.example.internmanagement.model.User;
import com.example.internmanagement.util.FinalEvaluationRules;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/mentor/final-evaluation")
public class FinalEvaluationServlet extends HttpServlet {
    private final MentorFinalEvaluationDAO evaluationDAO = new MentorFinalEvaluationDAO();
    private final MentorReportDAO mentorReportDAO = new MentorReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentMentor(request, response);
        if (user == null) return;

        try {
            Long mentorId = evaluationDAO.findActiveMentorId(user.getId());
            if (mentorId == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Tài khoản chưa có hồ sơ mentor đang hoạt động.");
                return;
            }
            renderPage(request, response, user, mentorId,
                    positiveLong(request.getParameter("internId")), null, null);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải dữ liệu đánh giá tổng kết.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = currentMentor(request, response);
        if (user == null) return;

        try {
            Long mentorId = evaluationDAO.findActiveMentorId(user.getId());
            if (mentorId == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Tài khoản chưa có hồ sơ mentor đang hoạt động.");
                return;
            }
            Long parsedInternId = positiveLong(request.getParameter("internId"));
            if (parsedInternId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thực tập sinh không hợp lệ.");
                return;
            }
            long internId = parsedInternId;
            try {
                Integer knowledge = score(request.getParameter("knowledge"));
                Integer problemSolving = score(request.getParameter("problemSolving"));
                Integer initiative = score(request.getParameter("initiative"));
                Integer teamwork = score(request.getParameter("teamwork"));
                String comment = request.getParameter("generalComment");
                String action = request.getParameter("action");
                boolean finalize = "finalize".equals(action);
                if (!finalize && !"saveDraft".equals(action)) {
                    throw new IllegalArgumentException("Thao tác đánh giá không hợp lệ.");
                }
                int revision = requiredRevision(request.getParameter("revisionNo"));
                String status = evaluationDAO.save(mentorId, user.getId(), internId, revision,
                        knowledge, problemSolving, initiative, teamwork, comment, finalize,
                        request.getRemoteAddr());

                String result = "DRAFT".equals(status) ? "draft"
                        : "FINALIZED_AGAIN".equals(status) ? "refinalized" : "finalized";
                response.sendRedirect(request.getContextPath() + "/mentor/final-evaluation?internId="
                        + internId + "&success=" + result);
            } catch (IllegalArgumentException exception) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("errorMessage", exception.getMessage());
                renderPage(request, response, user, mentorId, internId, submittedValues(request), null);
            }
        } catch (SQLException exception) {
            throw new ServletException("Không thể lưu đánh giá tổng kết.", exception);
        }
    }

    private void renderPage(HttpServletRequest request, HttpServletResponse response,
                            User user, long mentorId, Long requestedInternId,
                            Map<String, Object> submitted, Map<String, Object> submittedFilter)
            throws SQLException, ServletException, IOException {
        List<Map<String, Object>> allInterns = evaluationDAO.assignedInterns(mentorId);
        String statusFilter = validStatusFilter(submittedFilter == null
                ? request.getParameter("status") : (String) submittedFilter.get("status"));
        boolean eligibleOnly = submittedFilter == null
                ? "true".equals(request.getParameter("eligibleOnly"))
                : Boolean.TRUE.equals(submittedFilter.get("eligibleOnly"));

        List<Map<String, Object>> visibleInterns = new ArrayList<>();
        for (Map<String, Object> intern : allInterns) {
            String evaluationStatus = (String) intern.get("evaluationStatus");
            if ("NOT_STARTED".equals(statusFilter) && evaluationStatus != null) continue;
            if (!"ALL".equals(statusFilter) && !"NOT_STARTED".equals(statusFilter)
                    && !statusFilter.equals(evaluationStatus)) continue;
            if (eligibleOnly && !Boolean.TRUE.equals(intern.get("eligible"))) continue;
            visibleInterns.add(intern);
        }

        Long selectedId = requestedInternId;
        boolean selectedVisible = false;
        if (selectedId != null) {
            for (Map<String, Object> intern : visibleInterns) {
                if (selectedId.equals(((Number) intern.get("id")).longValue())) {
                    selectedVisible = true;
                    break;
                }
            }
        }
        if (!selectedVisible) {
            selectedId = visibleInterns.isEmpty() ? null
                    : ((Number) visibleInterns.get(0).get("id")).longValue();
        }
        Long chosenId = selectedId;
        Map<String, Object> selectedIntern = selectedId == null ? null
                : allInterns.stream()
                .filter(intern -> chosenId.equals(((Number) intern.get("id")).longValue()))
                .findFirst().orElse(null);
        if (requestedInternId != null && selectedIntern == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Thực tập sinh không tồn tại hoặc không thuộc phạm vi hướng dẫn của bạn.");
            return;
        }

        Map<String, Object> evaluation = selectedId == null
                ? new LinkedHashMap<>() : evaluationDAO.findEvaluation(selectedId);
        if (submitted != null) {
            submitted.put("revisionNo", evaluation.getOrDefault("revisionNo", 0));
            submitted.put("status", evaluation.get("status"));
            submitted.put("finalizationCount", evaluation.getOrDefault("finalizationCount", 0));
            submitted.put("finalizedAt", evaluation.get("finalizedAt"));
            evaluation = submitted;
        }

        request.setAttribute("currentUser", user);
        request.setAttribute("recentNotifications", mentorReportDAO.recentNotifications(user.getId()));
        request.setAttribute("unreadNotificationCount", mentorReportDAO.unreadNotificationCount(user.getId()));
        request.setAttribute("interns", visibleInterns);
        request.setAttribute("selectedIntern", selectedIntern);
        request.setAttribute("selectedInternId", selectedId);
        request.setAttribute("evaluation", evaluation);
        request.setAttribute("canEvaluate", selectedIntern != null
                && Boolean.TRUE.equals(selectedIntern.get("eligible")));
        request.setAttribute("statusFilter", statusFilter);
        request.setAttribute("eligibleOnly", eligibleOnly);
        request.setAttribute("successMessage", successMessage(request.getParameter("success")));
        request.getRequestDispatcher("/WEB-INF/views/mentor-final-evaluation.jsp")
                .forward(request, response);
    }

    private User currentMentor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Object value = request.getSession(false) == null
                ? null : request.getSession(false).getAttribute("user");
        if (!(value instanceof User user) || !"MENTOR".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ mentor mới được truy cập chức năng đánh giá tổng kết.");
            return null;
        }
        return user;
    }

    private Map<String, Object> submittedValues(HttpServletRequest request) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("knowledgeScore", parseScoreForDisplay(request.getParameter("knowledge")));
        values.put("problemSolvingScore", parseScoreForDisplay(request.getParameter("problemSolving")));
        values.put("initiativeScore", parseScoreForDisplay(request.getParameter("initiative")));
        values.put("teamworkScore", parseScoreForDisplay(request.getParameter("teamwork")));
        values.put("overallComments", request.getParameter("generalComment"));
        return values;
    }

    private Integer parseScoreForDisplay(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer score(String value) {
        Integer score = parseScoreForDisplay(value);
        if (value != null && !value.isBlank() && score == null) {
            throw new IllegalArgumentException("Điểm từng tiêu chí phải là số nguyên từ 1 đến 5.");
        }
        return score;
    }

    private Long positiveLong(String value) {
        try {
            long id = Long.parseLong(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private long requiredPositiveLong(String value, String field) {
        Long id = positiveLong(value);
        if (id == null) throw new IllegalArgumentException(field + " không hợp lệ.");
        return id;
    }

    private int requiredRevision(String value) {
        try {
            int revision = Integer.parseInt(value);
            if (revision >= 0) return revision;
        } catch (NumberFormatException ignored) {
            // Return the same user-facing validation error for missing and malformed revisions.
        }
        throw new IllegalArgumentException("Phiên bản đánh giá không hợp lệ. Hãy tải lại trang.");
    }

    private String validStatusFilter(String value) {
        return FinalEvaluationRules.statusFilter(value);
    }

    private String successMessage(String value) {
        return switch (value == null ? "" : value) {
            case "draft" -> "Đã lưu bản nháp vào cơ sở dữ liệu.";
            case "finalized" -> "Đã chốt đánh giá và lưu vào cơ sở dữ liệu.";
            case "refinalized" -> "Đã chốt lại đánh giá; phiên bản trước được lưu trong lịch sử.";
            default -> null;
        };
    }
}
