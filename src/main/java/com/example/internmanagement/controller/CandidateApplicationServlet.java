package com.example.internmanagement.controller;

import com.example.internmanagement.dao.CandidateDAO;
import com.example.internmanagement.model.Candidate;
import com.example.internmanagement.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/applications", "/candidate-files/*"})
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 11 * 1024 * 1024)
public class CandidateApplicationServlet extends HttpServlet {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");
    private final CandidateDAO candidateDAO = new CandidateDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/candidate-files".equals(request.getServletPath())) {
            serveCandidateFile(request, response);
            return;
        }

        User user = currentUser(request);
        if (!hasRole(user, "CANDIDATE")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            renderApplication(request, response, candidateDAO.findByUserId(user.getId()), null);
        } catch (SQLException e) {
            throw new ServletException("Không thể tải hồ sơ ứng tuyển.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = currentUser(request);
        if (!hasRole(user, "CANDIDATE")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Path savedFile = null;
        try {
            Candidate existing = candidateDAO.findByUserId(user.getId());
            Candidate candidate = new Candidate();
            candidate.setUserId(user.getId());
            candidate.setFullName(user.getFull_name());
            candidate.setEmail(user.getEmail());
            candidate.setPhone(required(request, "phone", "Vui lòng nhập số điện thoại."));
            candidate.setUniversityName(required(request, "universityName", "Vui lòng nhập trường đại học."));
            candidate.setMajorName(required(request, "majorName", "Vui lòng nhập chuyên ngành."));
            candidate.setDesiredPosition(required(request, "desiredPosition", "Vui lòng nhập vị trí ứng tuyển."));

            requireLength(candidate.getPhone(), 20, "Số điện thoại quá dài.");
            if (!PHONE_PATTERN.matcher(candidate.getPhone()).matches()) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
            }
            requireLength(candidate.getUniversityName(), 150, "Tên trường không được vượt quá 150 ký tự.");
            requireLength(candidate.getMajorName(), 100, "Tên chuyên ngành không được vượt quá 100 ký tự.");
            requireLength(candidate.getDesiredPosition(), 100, "Vị trí ứng tuyển không được vượt quá 100 ký tự.");

            Part cv = request.getPart("cv");
            if (cv != null && cv.getSize() > 0) {
                validatePdf(cv);
                String storedName = UUID.randomUUID() + ".pdf";
                Path directory = uploadDirectory();
                Files.createDirectories(directory);
                savedFile = directory.resolve(storedName);
                try (InputStream input = cv.getInputStream()) {
                    Files.copy(input, savedFile);
                }
                candidate.setCvFileUrl("/candidate-files/" + storedName);
            } else if (existing != null && existing.getCvFileUrl() != null) {
                candidate.setCvFileUrl(existing.getCvFileUrl());
            } else {
                throw new IllegalArgumentException("Vui lòng tải lên CV định dạng PDF.");
            }

            candidateDAO.save(candidate);
            response.sendRedirect(request.getContextPath() + "/applications?submitted=1");
        } catch (IllegalArgumentException | IllegalStateException e) {
            deleteQuietly(savedFile);
            Candidate existing = loadExisting(user, e);
            renderApplication(request, response, existing, e.getMessage());
        } catch (SQLException e) {
            deleteQuietly(savedFile);
            throw new ServletException("Không thể lưu hồ sơ ứng tuyển.", e);
        } catch (IOException e) {
            deleteQuietly(savedFile);
            throw e;
        }
    }

    private Candidate loadExisting(User user, Exception original) throws ServletException {
        try {
            return candidateDAO.findByUserId(user.getId());
        } catch (SQLException e) {
            e.addSuppressed(original);
            throw new ServletException("Không thể tải lại hồ sơ ứng tuyển.", e);
        }
    }

    private void renderApplication(HttpServletRequest request, HttpServletResponse response,
                                   Candidate candidate, String error)
            throws ServletException, IOException {
        request.setAttribute("application", candidate);
        request.setAttribute("errorMessage", error);
        if ("1".equals(request.getParameter("submitted"))) {
            request.setAttribute("successMessage", "Hồ sơ đã được nộp thành công. Bạn có thể theo dõi trạng thái xét duyệt tại đây.");
        }
        request.getRequestDispatcher("/WEB-INF/views/candidate-application.jsp").forward(request, response);
    }

    private void serveCandidateFile(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/[0-9a-fA-F-]{36}\\.pdf")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            String fileUrl = "/candidate-files" + pathInfo;
            Candidate candidate = candidateDAO.findByFileUrl(fileUrl);
            if (candidate == null || !canViewCandidate(request, candidate)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Path directory = uploadDirectory();
            Path file = directory.resolve(pathInfo.substring(1)).normalize();
            if (!file.startsWith(directory) || !Files.isRegularFile(file)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"CV.pdf\"");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setContentLengthLong(Files.size(file));
            Files.copy(file, response.getOutputStream());
        } catch (SQLException e) {
            throw new ServletException("Không thể mở CV ứng tuyển.", e);
        }
    }

    private boolean canViewCandidate(HttpServletRequest request, Candidate candidate) {
        User user = currentUser(request);
        return hasRole(user, "HR") || hasRole(user, "ADMIN")
                || (hasRole(user, "CANDIDATE") && user.getId() == candidate.getUserId());
    }

    private void validatePdf(Part part) throws IOException {
        String name = part.getSubmittedFileName();
        if (name == null || !name.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("CV phải có định dạng PDF.");
        }
        try (InputStream input = part.getInputStream()) {
            byte[] signature = input.readNBytes(5);
            if (!"%PDF-".equals(new String(signature, java.nio.charset.StandardCharsets.US_ASCII))) {
                throw new IllegalArgumentException("Tệp tải lên không phải PDF hợp lệ.");
            }
        }
    }

    private String required(HttpServletRequest request, String name, String message) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void requireLength(String value, int maxLength, String message) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
    }

    private Path uploadDirectory() {
        return Paths.get(System.getProperty("catalina.base", System.getProperty("java.io.tmpdir")),
                "intern-uploads").toAbsolutePath().normalize();
    }

    private void deleteQuietly(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                getServletContext().log("Không thể xóa tệp CV không được lưu: " + file, e);
            }
        }
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    private boolean hasRole(User user, String role) {
        return user != null && role.equalsIgnoreCase(user.getRole());
    }
}
