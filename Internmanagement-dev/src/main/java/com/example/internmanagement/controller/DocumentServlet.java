package com.example.internmanagement.controller;

import com.example.internmanagement.dao.InternDocumentDAO;
import com.example.internmanagement.model.InternDocument;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@WebServlet({"/documents/upload", "/documents/review", "/uploads/*"})
@MultipartConfig(
    maxFileSize = 10 * 1024 * 1024,   // 10 MB
    maxRequestSize = 22 * 1024 * 1024 // 22 MB
)
public class DocumentServlet extends HttpServlet {

    private final InternDocumentDAO dao = new InternDocumentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        
        if (servletPath.equals("/uploads")) {
            serveUpload(request, response);
        } else if (servletPath.endsWith("upload")) {
            // Intern: load their own profile for the internId hidden field
            com.example.internmanagement.model.User sessionUser = 
                (com.example.internmanagement.model.User) request.getSession().getAttribute("user");
            if (sessionUser != null) {
                try {
                    com.example.internmanagement.dao.InternProfileDAO profileDAO =
                        new com.example.internmanagement.dao.InternProfileDAO();
                    com.example.internmanagement.model.InternProfile myProfile =
                        profileDAO.findByUserId(sessionUser.getId());
                    if (myProfile != null) {
                        request.setAttribute("internProfile", myProfile);
                    }
                } catch (java.sql.SQLException e) { /* non-fatal */ }
            }
            request.getRequestDispatcher("/WEB-INF/views/dashboard-intern.jsp")
                   .forward(request, response);
        } else {
            // HR: load pending documents
            try {
                java.util.List<java.util.Map<String,String>> pendingDocs = new java.util.ArrayList<>();
                String sql = "SELECT d.id, d.document_type, d.file_name, d.file_url, " +
                             "d.approval_status, d.uploaded_at, d.intern_profile_id, " +
                             "ip.intern_code, u.full_name AS intern_full_name " +
                             "FROM intern_documents d " +
                             "JOIN intern_profiles ip ON ip.id = d.intern_profile_id " +
                             "JOIN users u ON u.id = ip.user_id " +
                             "WHERE d.approval_status = 'PENDING' " +
                             "ORDER BY d.uploaded_at DESC";
                try (java.sql.Connection conn = com.example.internmanagement.util.DBConnection.getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                     java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.util.Map<String,String> row = new java.util.HashMap<>();
                        row.put("id", rs.getString("id"));
                        row.put("documentType", rs.getString("document_type"));
                        row.put("fileName", rs.getString("file_name"));
                        row.put("fileUrl", rs.getString("file_url"));
                        row.put("approvalStatus", rs.getString("approval_status"));
                        row.put("uploadedAt", rs.getString("uploaded_at"));
                        row.put("internProfileId", rs.getString("intern_profile_id"));
                        row.put("internCode", rs.getString("intern_code"));
                        row.put("internFullName", rs.getString("intern_full_name"));
                        pendingDocs.add(row);
                    }
                }
                request.setAttribute("pendingDocuments", pendingDocs);
            } catch (java.sql.SQLException e) { /* non-fatal */ }
            request.getRequestDispatcher("/WEB-INF/views/screens/document-review.jsp")
                   .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String servletPath = request.getServletPath();

        if (servletPath.endsWith("review")) {
            handleReview(request, response);
        } else {
            handleUpload(request, response);
        }
    }

    private void handleReview(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        try {
            requireRole(request, "HR", "ADMIN");
            int documentId = Integer.parseInt(request.getParameter("documentId"));
            String status = request.getParameter("status");
            if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
                throw new IllegalArgumentException("Trạng thái duyệt tài liệu không hợp lệ.");
            }
            String reviewNote = request.getParameter("reviewNote");
            String internId = request.getParameter("internId");

            // Lấy reviewer từ session
            com.example.internmanagement.model.User sessionUser =
                (com.example.internmanagement.model.User) request.getSession().getAttribute("user");
            Long reviewerId = (sessionUser != null) ? (long) sessionUser.getId() : null;

            try (java.sql.Connection connection = com.example.internmanagement.util.DBConnection.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    boolean changed = dao.review(connection, documentId, status, reviewNote, reviewerId);
                    if (!changed) throw new IllegalStateException("Hồ sơ đã được xử lý trước đó.");
                    connection.commit();
                } catch (Exception error) {
                    connection.rollback(); throw error;
                } finally { connection.setAutoCommit(true); }
            }

            // Nếu duyệt từ trang profile intern, trở về profile; nếu từ trang review → về review
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("/interns/profile")) {
                response.sendRedirect(request.getContextPath() + "/interns/profile?id=" + internId + "&reviewed=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/documents/review?reviewed=1");
            }

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi xử lý duyệt tài liệu: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/screens/document-review.jsp")
                   .forward(request, response);
        }
    }

    private void handleUpload(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        try {
            requireRole(request, "INTERN");
            com.example.internmanagement.model.User sessionUser =
                    (com.example.internmanagement.model.User) request.getSession().getAttribute("user");
            com.example.internmanagement.model.InternProfile profile =
                    new com.example.internmanagement.dao.InternProfileDAO().findByUserId(sessionUser.getId());
            if (profile == null) {
                throw new IllegalArgumentException("Tài khoản chưa có hồ sơ thực tập sinh.");
            }
            int internProfileId = profile.getId();
            String documentType = request.getParameter("documentType");
            if (!"CV".equals(documentType) && !"APPLICATION_LETTER".equals(documentType)
                    && !"RECOMMENDATION_LETTER".equals(documentType) && !"WEEKLY_REPORT".equals(documentType)) {
                throw new IllegalArgumentException("Loại tài liệu không hợp lệ.");
            }

            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                throw new IllegalArgumentException("Vui lòng chọn tệp cần tải lên");
            }

            String originalFileName = filePart.getSubmittedFileName();
            if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("Chỉ chấp nhận tệp định dạng PDF");
            }

            String cleanedOriginalName = Paths.get(originalFileName).getFileName().toString();
            String storedFileName = UUID.randomUUID() + ".pdf";

            // Lưu tệp vào thư mục intern-uploads
            Path uploadDir = Paths.get(System.getProperty("catalina.base", System.getProperty("java.io.tmpdir")), "intern-uploads");
            Files.createDirectories(uploadDir);

            try (InputStream inputStream = filePart.getInputStream()) {
                Files.copy(inputStream, uploadDir.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);
            }

            // Đường dẫn URL tương đối để truy cập file
            String fileUrl = "/uploads/" + storedFileName;

            // Lưu thông tin vào Database khớp với Model InternDocument
            InternDocument document = new InternDocument();
            document.setInternProfileId(internProfileId);
            document.setDocumentType(documentType);
            document.setFileName(cleanedOriginalName);
            document.setFileUrl(fileUrl);
            document.setFileSizeBytes(filePart.getSize());
            document.setUploadedAt(LocalDateTime.now());

            dao.add(document);

            response.sendRedirect(request.getContextPath() + "/home?uploaded=1");

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/dashboard-intern.jsp")
                   .forward(request, response);
        }
    }

    private void serveUpload(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/[0-9a-fA-F-]{36}\\.pdf")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            InternDocument document = dao.findByFileUrl("/uploads" + pathInfo);
            if (document == null || !canViewDocument(request, document)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            Path uploadDir = uploadDirectory();
            Path file = uploadDir.resolve(pathInfo.substring(1)).normalize();
            if (!file.startsWith(uploadDir) || !Files.isRegularFile(file)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"" + document.getFileName().replace("\"", "") + "\"");
            response.setContentLengthLong(Files.size(file));
            Files.copy(file, response.getOutputStream());
        } catch (java.sql.SQLException e) {
            throw new ServletException("Không thể mở tài liệu.", e);
        }
    }

    private boolean canViewDocument(HttpServletRequest request, InternDocument document) throws java.sql.SQLException {
        com.example.internmanagement.model.User user =
                (com.example.internmanagement.model.User) request.getSession().getAttribute("user");
        if (user == null) return false;
        if ("HR".equalsIgnoreCase(user.getRole()) || "ADMIN".equalsIgnoreCase(user.getRole())) return true;
        if (!"INTERN".equalsIgnoreCase(user.getRole())) return false;
        com.example.internmanagement.model.InternProfile profile =
                new com.example.internmanagement.dao.InternProfileDAO().findByUserId(user.getId());
        return profile != null && profile.getId() == document.getInternProfileId();
    }

    private Path uploadDirectory() {
        return Paths.get(System.getProperty("catalina.base", System.getProperty("java.io.tmpdir")), "intern-uploads");
    }

    private void requireRole(HttpServletRequest request, String... roles) throws ServletException {
        com.example.internmanagement.model.User user =
                (com.example.internmanagement.model.User) request.getSession().getAttribute("user");
        if (user != null) {
            for (String role : roles) {
                if (role.equalsIgnoreCase(user.getRole())) return;
            }
        }
        throw new ServletException("Bạn không có quyền sử dụng chức năng này.");
    }
}
