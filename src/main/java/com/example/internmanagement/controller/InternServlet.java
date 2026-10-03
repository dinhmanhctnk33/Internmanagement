package com.example.internmanagement.controller;

import com.example.internmanagement.dao.InternDocumentDAO;
import com.example.internmanagement.dao.InternProfileDAO;
import com.example.internmanagement.model.InternProfile;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet({"/home", "/interns", "/interns/new", "/interns/edit", "/interns/profile"})
public class InternServlet extends HttpServlet {

    private static final Pattern VIETNAM_PHONE_PATTERN = Pattern.compile("^0[0-9]{9}$");
    private final InternProfileDAO profileDAO = new InternProfileDAO();
    private final InternDocumentDAO documentDAO = new InternDocumentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();

        try {
            switch (path) {
                case "/home":
                    renderHome(request, response);
                    break;
                case "/interns":
                    renderInternList(request, response);
                    break;
                case "/interns/profile":
                    renderInternProfile(request, response);
                    break;
                case "/interns/edit":
                    renderInternEdit(request, response);
                    break;
                case "/interns/new":
                    renderInternCreate(request, response);
                    break;
                default:
                    renderHome(request, response);
                    break;
            }
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException | IllegalArgumentException e) {
            throw new ServletException("Lỗi hệ thống khi tải trang thực tập sinh", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!isHrOrAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ HR hoặc Admin mới có thể tạo và chỉnh sửa hồ sơ thực tập sinh.");
            return;
        }

        InternProfile profile = null;
        try {
            profile = parseProfileFromRequest(request);
            if (profile.getId() > 0) {
                profileDAO.update(profile);
            } else {
                profileDAO.insert(profile);
            }

            response.sendRedirect(request.getContextPath() + "/interns/profile?id=" + profile.getId() + "&success=1");

        } catch (SQLException | IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            if (profile == null) {
                profile = new InternProfile();
            }
            request.setAttribute("intern", profile);
            try {
                request.setAttribute("internAccounts", profileDAO.findSelectableInternUsers(profile.getUserId()));
            } catch (SQLException ignored) {
                request.setAttribute("internAccounts", java.util.List.of());
            }
            request.getRequestDispatcher("/WEB-INF/views/screens/intern-form.jsp")
                   .forward(request, response);
        }
    }

    // --- Các hàm điều hướng GET ---

    private void renderHome(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        com.example.internmanagement.model.User user = (com.example.internmanagement.model.User) request.getSession().getAttribute("user");
        String role = (user != null && user.getRole() != null) ? user.getRole() : "INTERN";

        if ("ADMIN".equalsIgnoreCase(role)) {
            // Load danh sách user với role_code để dashboard-admin.jsp hiển thị
            java.util.List<java.util.Map<String,String>> rows = new java.util.ArrayList<>();
            String sql = "SELECT u.id, u.username, u.full_name, u.email, u.status, " +
                         "COALESCE(r.role_code,'UNASSIGNED') role_code " +
                         "FROM users u " +
                         "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
                         "LEFT JOIN roles r ON r.id = ur.role_id " +
                         "ORDER BY u.id DESC";
            try (java.sql.Connection conn = com.example.internmanagement.util.DBConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String,String> row = new java.util.HashMap<>();
                    row.put("id",        rs.getString("id"));
                    row.put("username",  rs.getString("username"));
                    row.put("full_name", rs.getString("full_name"));
                    row.put("email",     rs.getString("email"));
                    row.put("status",    rs.getString("status"));
                    row.put("role_code", rs.getString("role_code"));
                    rows.add(row);
                }
            } catch (java.sql.SQLException e) {
                throw new ServletException("Không thể tải danh sách người dùng", e);
            }
            request.setAttribute("users", rows);
            request.getRequestDispatcher("/WEB-INF/views/dashboard-admin.jsp").forward(request, response);
        } else if ("INTERN".equalsIgnoreCase(role)) {
            if (user != null) {
                InternProfile profile = profileDAO.findByUserId(user.getId());
                request.setAttribute("internProfile", profile);
                if (profile != null) {
                    List<com.example.internmanagement.model.InternDocument> documents =
                            documentDAO.findByIntern(profile.getId());
                    request.setAttribute("documents", documents);
                    request.setAttribute("hasCv", documents.stream()
                            .anyMatch(document -> "CV".equals(document.getDocumentType())));
                    request.setAttribute("hasLetter", documents.stream()
                            .anyMatch(document -> "APPLICATION_LETTER".equals(document.getDocumentType())));
                    request.setAttribute("hasRecommendation", documents.stream()
                            .anyMatch(document -> "RECOMMENDATION_LETTER".equals(document.getDocumentType())));
                    request.setAttribute("hasWeeklyReport", documents.stream()
                            .anyMatch(document -> "WEEKLY_REPORT".equals(document.getDocumentType())));
                }
            }
            request.getRequestDispatcher("/WEB-INF/views/dashboard-intern.jsp").forward(request, response);
        } else {
            String keyword = request.getParameter("q");
            String university = request.getParameter("university");
            String major = request.getParameter("major");
            request.setAttribute("interns", profileDAO.searchInterns(keyword, university, major));
            request.setAttribute("universities", profileDAO.findDistinctUniversities());
            request.setAttribute("majors", profileDAO.findDistinctMajors());
            request.setAttribute("pendingDocuments", loadPendingDocuments());
            request.setAttribute("dashboardStats", loadHrDashboardStats());
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        }
    }

    private java.util.Map<String, Integer> loadHrDashboardStats() throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM intern_profiles) AS total_interns,
                    (SELECT COUNT(*) FROM intern_profiles WHERE internship_status = 'ACTIVE') AS active_interns,
                    (SELECT COUNT(DISTINCT NULLIF(TRIM(university_name), '')) FROM intern_profiles) AS university_count,
                    (SELECT COUNT(*) FROM intern_documents WHERE approval_status = 'PENDING') AS pending_documents
                """;
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        try (java.sql.Connection conn = com.example.internmanagement.util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("totalInterns", rs.getInt("total_interns"));
                stats.put("activeInterns", rs.getInt("active_interns"));
                stats.put("universityCount", rs.getInt("university_count"));
                stats.put("pendingDocuments", rs.getInt("pending_documents"));
            }
        }
        return stats;
    }

    private java.util.List<java.util.Map<String, String>> loadPendingDocuments() throws SQLException {
        String sql = """
                SELECT d.id, d.document_type, d.file_name, d.file_url, d.uploaded_at,
                       d.intern_profile_id, ip.intern_code, u.full_name AS intern_full_name
                FROM intern_documents d
                JOIN intern_profiles ip ON ip.id = d.intern_profile_id
                JOIN users u ON u.id = ip.user_id
                WHERE d.approval_status = 'PENDING'
                ORDER BY d.uploaded_at DESC
                """;
        java.util.List<java.util.Map<String, String>> documents = new java.util.ArrayList<>();
        try (java.sql.Connection conn = com.example.internmanagement.util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, String> document = new java.util.HashMap<>();
                document.put("id", rs.getString("id"));
                document.put("documentType", rs.getString("document_type"));
                document.put("fileName", rs.getString("file_name"));
                document.put("fileUrl", rs.getString("file_url"));
                document.put("uploadedAt", rs.getString("uploaded_at"));
                document.put("internProfileId", rs.getString("intern_profile_id"));
                document.put("internCode", rs.getString("intern_code"));
                document.put("internFullName", rs.getString("intern_full_name"));
                documents.add(document);
            }
        }
        return documents;
    }

    private void renderInternList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        requireHrOrAdmin(request);
        String keyword = request.getParameter("q");
        String university = request.getParameter("university");
        String major = request.getParameter("major");
        List<InternProfile> list = profileDAO.searchInterns(keyword, university, major);

        request.setAttribute("interns", list);
        request.setAttribute("universities", profileDAO.findDistinctUniversities());
        request.setAttribute("majors", profileDAO.findDistinctMajors());
        request.getRequestDispatcher("/WEB-INF/views/screens/intern-list.jsp")
               .forward(request, response);
    }

    private void renderInternProfile(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        com.example.internmanagement.model.User sessionUser =
                (com.example.internmanagement.model.User) request.getSession().getAttribute("user");
        boolean internUser = sessionUser != null && "INTERN".equalsIgnoreCase(sessionUser.getRole());
        String idParam = request.getParameter("id");
        InternProfile intern = null;
        if (idParam != null && !idParam.isBlank()) {
            int internId = Integer.parseInt(idParam);
            intern = profileDAO.getInternById(internId);
            if (internUser && (intern == null || intern.getUserId() != sessionUser.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn chỉ có thể xem hồ sơ của chính mình.");
                return;
            }
        } else {
            // Intern viewing their own profile - find by user session
            if (sessionUser != null) {
                intern = profileDAO.findByUserId(sessionUser.getId());
            }
        }
        if (intern != null) {
            request.setAttribute("intern", intern);
            request.setAttribute("documents", documentDAO.findByIntern(intern.getId()));
        }
        request.getRequestDispatcher("/WEB-INF/views/screens/intern-profile.jsp")
               .forward(request, response);
    }

    private void renderInternEdit(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        requireHrOrAdmin(request);
        int internId = parseId(request);
        InternProfile intern = profileDAO.getInternById(internId);
        request.setAttribute("intern", intern);
        request.setAttribute("internAccounts", profileDAO.findSelectableInternUsers(intern == null ? 0 : intern.getUserId()));

        request.getRequestDispatcher("/WEB-INF/views/screens/intern-form.jsp")
               .forward(request, response);
    }

    private void renderInternCreate(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        requireHrOrAdmin(request);
        request.setAttribute("internAccounts", profileDAO.findSelectableInternUsers(0));
        request.getRequestDispatcher("/WEB-INF/views/screens/intern-form.jsp")
               .forward(request, response);
    }

    // --- Các hàm Helper bổ trợ ---

    private InternProfile parseProfileFromRequest(HttpServletRequest request) throws SQLException {
        String rawId = request.getParameter("id");
        InternProfile profile;
        if (rawId != null && !rawId.isBlank()) {
            int id = Integer.parseInt(rawId);
            profile = profileDAO.getInternById(id);
            if (profile == null) {
                throw new IllegalArgumentException("Không tìm thấy hồ sơ thực tập sinh cần cập nhật.");
            }
        } else {
            profile = new InternProfile();
            profile.setUserId(parseOptionalId(request.getParameter("userId"), "Tài khoản thực tập sinh"));
        }

        setIfPresent(request, "internCode", profile::setInternCode);
        setIfPresent(request, "phoneNumber", profile::setPhoneNumber);
        setIfPresent(request, "universityName", profile::setUniversityName);
        setIfPresent(request, "majorName", profile::setMajorName);
        setIfPresent(request, "internshipStatus", profile::setInternshipStatus);

        if (isBlank(profile.getInternCode())) {
            throw new IllegalArgumentException("Mã thực tập sinh là bắt buộc.");
        }

        if (!isBlank(profile.getPhoneNumber())
                && !VIETNAM_PHONE_PATTERN.matcher(profile.getPhoneNumber()).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.");
        }

        return profile;
    }

    private void setIfPresent(
            HttpServletRequest request,
            String parameterName,
            java.util.function.Consumer<String> setter) {
        String value = request.getParameter(parameterName);
        if (value != null) {
            setter.accept(value.trim());
        }
    }

    private int parseOptionalId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " là bắt buộc.");
        }
        try {
            int id = Integer.parseInt(value);
            if (id <= 0) {
                throw new IllegalArgumentException(fieldName + " không hợp lệ.");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.", e);
        }
    }

    private void setFormAttributes(HttpServletRequest request, InternProfile profile) {
        request.setAttribute("internCode", profile.getInternCode());
        request.setAttribute("phoneNumber", profile.getPhoneNumber());
        request.setAttribute("universityName", profile.getUniversityName());
        request.setAttribute("majorName", profile.getMajorName());
        request.setAttribute("internshipStatus", profile.getInternshipStatus());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int parseId(HttpServletRequest request) {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            throw new IllegalArgumentException("Thiếu tham số ID thực tập sinh");
        }
        return Integer.parseInt(idParam);
    }

    private boolean isHrOrAdmin(HttpServletRequest request) {
        com.example.internmanagement.model.User user =
                (com.example.internmanagement.model.User) request.getSession().getAttribute("user");
        return user != null && ("HR".equalsIgnoreCase(user.getRole())
                || "ADMIN".equalsIgnoreCase(user.getRole()));
    }

    private void requireHrOrAdmin(HttpServletRequest request) {
        if (!isHrOrAdmin(request)) {
            throw new SecurityException("Chỉ HR hoặc Admin mới có quyền sử dụng chức năng này.");
        }
    }

}
