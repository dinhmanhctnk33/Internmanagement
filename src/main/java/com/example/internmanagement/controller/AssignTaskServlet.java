package com.example.internmanagement.controller;

import com.example.internmanagement.dao.InternProfileDAO;
import com.example.internmanagement.dao.TaskDAO;
import com.example.internmanagement.model.InternProfile;
import com.example.internmanagement.model.User;

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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/mentor/tasks/assign")
public class AssignTaskServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AssignTaskServlet.class.getName());
    private final InternProfileDAO internProfileDAO = new InternProfileDAO();
    private final TaskDAO taskDAO = new TaskDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User mentor = requireMentor(request, response);
        if (mentor == null) {
            return;
        }
        if ("assigned".equals(request.getParameter("success"))) {
            request.setAttribute("successMessage", "Đã giao nhiệm vụ thành công.");
        }
        renderForm(request, response, mentor, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User mentor = requireMentor(request, response);
        if (mentor == null) {
            return;
        }

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDateValue = request.getParameter("dueDate");
        List<Integer> selectedInterns = parseInternIds(request.getParameterValues("internIds"));
        LocalDate dueDate = null;

        if (title == null || title.isBlank()) {
            request.setAttribute("titleError", "Tiêu đề không được để trống.");
        } else if (title.trim().length() > 200) {
            request.setAttribute("titleError", "Tiêu đề không được vượt quá 200 ký tự.");
        }
        if (description != null && description.length() > 2000) {
            request.setAttribute("descriptionError", "Mô tả không được vượt quá 2000 ký tự.");
        }
        if (dueDateValue != null && !dueDateValue.isBlank()) {
            try {
                dueDate = LocalDate.parse(dueDateValue.trim());
                if (dueDate.isBefore(LocalDate.now())) {
                    request.setAttribute("dueDateError", "Hạn hoàn thành không được trước ngày hôm nay.");
                }
            } catch (DateTimeParseException exception) {
                request.setAttribute("dueDateError", "Định dạng ngày không hợp lệ.");
            }
        }
        if (selectedInterns.isEmpty()) {
            request.setAttribute("internIdsError", "Cần chọn ít nhất một thực tập sinh nhận việc.");
        }

        if (hasValidationErrors(request)) {
            renderForm(request, response, mentor, new LinkedHashSet<>(selectedInterns));
            return;
        }

        try {
            taskDAO.createTasksForInterns(title, description, dueDate, mentor.getId(), selectedInterns);
            response.sendRedirect(request.getContextPath() + "/mentor/tasks/assign?success=assigned");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("internIdsError", "Bạn chỉ có thể giao việc cho thực tập sinh thuộc nhóm của mình.");
            renderForm(request, response, mentor, new LinkedHashSet<>(selectedInterns));
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Unable to assign mentor task", exception);
            request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu: Không thể tạo nhiệm vụ. Vui lòng thử lại.");
            renderForm(request, response, mentor, new LinkedHashSet<>(selectedInterns));
        }
    }

    private User requireMentor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Object value = session.getAttribute("currentUser");
        if (!(value instanceof User)) {
            value = session.getAttribute("user");
        }
        String role = (String) session.getAttribute("userRole");
        if (!(value instanceof User user) || !"MENTOR".equalsIgnoreCase(role == null ? user.getRoleCode() : role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response, User mentor,
                            Set<Integer> selectedInterns) throws ServletException, IOException {
        try {
            List<InternProfile> interns = internProfileDAO.findActiveByMentorUserId(mentor.getId());
            request.setAttribute("internList", interns);
            request.setAttribute("todayDate", LocalDate.now().toString());
            if (selectedInterns != null) {
                request.setAttribute("selectedInterns", selectedInterns);
            }
            request.getRequestDispatcher("/WEB-INF/views/mentor-assign-task.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Unable to load mentor's interns", exception);
        }
    }

    private List<Integer> parseInternIds(String[] values) {
        Set<Integer> ids = new LinkedHashSet<>();
        if (values != null) {
            for (String value : values) {
                try {
                    ids.add(Integer.valueOf(value));
                } catch (NumberFormatException exception) {
                    return List.of();
                }
            }
        }
        return new ArrayList<>(ids);
    }

    private boolean hasValidationErrors(HttpServletRequest request) {
        return request.getAttribute("titleError") != null
                || request.getAttribute("descriptionError") != null
                || request.getAttribute("dueDateError") != null
                || request.getAttribute("internIdsError") != null;
    }
}