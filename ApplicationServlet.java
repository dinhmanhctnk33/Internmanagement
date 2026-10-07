package com.example.internmanagement.controller;

import com.example.internmanagement.dao.ApplicationDAO;
import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet({"/applications", "/my-applications"})
public class ApplicationServlet extends HttpServlet {

    private final ApplicationDAO applicationDAO = new ApplicationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();
        String role = roleCode(user);
        try {
            request.setAttribute("role", role);
            if ("/applications".equals(path)) {
                if (!isHr(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                request.setAttribute("page", "applications");
                request.setAttribute("programs", applicationDAO.findPrograms());
                request.setAttribute("applications", applicationDAO.findForHr(
                        request.getParameter("status"), request.getParameter("q")));
            } else {
                if (!isApplicant(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                request.setAttribute("page", "my-applications");
                request.setAttribute("programs", applicationDAO.findPrograms());
                request.setAttribute("myApplications", applicationDAO.findMine(user.getId()));
            }
            request.getRequestDispatcher("/WEB-INF/views/business.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Không thể tải hồ sơ ứng tuyển.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = roleCode(user);
        String path = request.getServletPath();
        String action = request.getParameter("action");
        try (Connection connection = DBConnection.getConnection()) {
            if ("/applications".equals(path) && "decide".equals(action)) {
                if (!isHr(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                long applicationId = Long.parseLong(request.getParameter("applicationId"));
                boolean saved = applicationDAO.decide(connection, applicationId, user.getId(),
                        request.getParameter("decision"), request.getParameter("reason"));
                if (!saved) {
                    redirectWithMessage(request, response, "/applications",
                            "Hồ sơ đã được xử lý hoặc không còn ở trạng thái chờ.");
                    return;
                }
                redirectWithMessage(request, response, "/applications",
                        "Quyết định đã được lưu.");
                return;
            }

            if (!"/my-applications".equals(path) || !isApplicant(role)
                    || !("save-application".equals(action) || "submit-application".equals(action))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            int programId = Integer.parseInt(request.getParameter("programId"));
            String university = value(request, "university");
            String major = value(request, "major");
            Integer studyYear = parseOptionalInteger(request.getParameter("studyYear"), "Năm học");
            Double gpa = parseOptionalDouble(request.getParameter("gpa"), "GPA");
            boolean submit = "submit-application".equals(action);
            if (submit && (university.isBlank() || major.isBlank())) {
                throw new IllegalArgumentException("Khi nộp hồ sơ cần có trường học và chuyên ngành.");
            }
            applicationDAO.save(connection, user.getId(), programId, university, major,
                    studyYear, gpa, value(request, "coverLetter"), submit);
            redirectWithMessage(request, response, "/my-applications",
                    submit ? "Hồ sơ đã được nộp." : "Bản nháp đã được lưu.");
        } catch (NumberFormatException e) {
            redirectWithMessage(request, response, path,
                    "Thông tin số hoặc mã chương trình không hợp lệ.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectWithMessage(request, response, path, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("Không thể lưu thay đổi hồ sơ ứng tuyển.", e);
        }
    }

    private static User currentUser(HttpServletRequest request) {
        Object value = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("user");
        return value instanceof User ? (User) value : null;
    }

    private static String roleCode(User user) {
        String role = user.getRoleCode();
        return role == null ? "" : role.toUpperCase();
    }

    private static boolean isHr(String role) {
        return "HR".equals(role) || "ADMIN".equals(role);
    }

    private static boolean isApplicant(String role) {
        return "CANDIDATE".equals(role) || "INTERN".equals(role);
    }

    private static String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private static Integer parseOptionalInteger(String rawValue, String label) {
        if (rawValue == null || rawValue.isBlank()) return null;
        try {
            int value = Integer.parseInt(rawValue);
            if (value < 1 || value > 8) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " phải nằm trong khoảng 1 đến 8.");
        }
    }

    private static Double parseOptionalDouble(String rawValue, String label) {
        if (rawValue == null || rawValue.isBlank()) return null;
        try {
            double value = Double.parseDouble(rawValue);
            if (!Double.isFinite(value) || value < 0 || value > 4) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " phải nằm trong khoảng 0 đến 4.");
        }
    }

    private static void redirectWithMessage(HttpServletRequest request, HttpServletResponse response,
                                             String path, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + path + "?message=" + encoded);
    }
}
