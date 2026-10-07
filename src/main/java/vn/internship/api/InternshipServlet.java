package vn.internship.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@MultipartConfig(
        fileSizeThreshold = 1_048_576,
        maxFileSize = 10_485_760,
        maxRequestSize = 11_534_336
)
public final class InternshipServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int VERIFICATION_EXPIRY_MINUTES = 30;
    private static final int VERIFICATION_WINDOW_HOURS = 1;
    private static final int MAX_VERIFICATION_EMAILS_PER_WINDOW = 3;
    private static final int MAX_CV_BYTES = 8 * 1024 * 1024;
    private static final int MAX_SUPPORTING_DOCUMENT_BYTES = 5 * 1024 * 1024;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private transient DataSource dataSource;
    private transient Session mailSession;
    private transient ScheduledExecutorService notificationWorker;
    private String verificationUrlBase;
    private String verificationFrom;

    @Override
    public void init() throws ServletException {
        try {
            InitialContext context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/InternshipDB");
            mailSession = (Session) context.lookup("java:comp/env/mail/InternshipMail");
            verificationUrlBase = getServletConfig().getInitParameter("verificationUrlBase");
            verificationFrom = getServletConfig().getInitParameter("verificationFrom");
            if (verificationUrlBase == null || verificationUrlBase.isBlank()
                    || verificationFrom == null || verificationFrom.isBlank()) {
                throw new ServletException("Configure verificationUrlBase and verificationFrom.");
            }
            if ("true".equalsIgnoreCase(
                    getServletConfig().getInitParameter("installSchema"))) {
                DatabaseSchema.install(dataSource);
            }
            notificationWorker = Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "internship-email-outbox");
                thread.setDaemon(true);
                return thread;
            });
            notificationWorker.scheduleWithFixedDelay(
                    this::deliverPendingNotifications, 0, 60, TimeUnit.SECONDS);
        } catch (NamingException | SQLException e) {
            throw new ServletException("Unable to initialize database or mail resources.", e);
        }
    }

    private String applicationStatus(long accountId, long programId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status FROM internship_applications "
                             + "WHERE account_id = ? AND program_id = ?")) {
            statement.setLong(1, accountId);
            statement.setLong(2, programId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Application status could not be read.");
                }
                return result.getString(1);
            }
        }
    }

    @Override
    public void destroy() {
        if (notificationWorker != null) {
            notificationWorker.shutdownNow();
        }
        super.destroy();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        try {
            route(request, response);
        } catch (ApiException e) {
            sendError(response, e.status, e.getMessage());
        } catch (SQLException | GeneralSecurityException e) {
            getServletContext().log("Internship API request failed.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The request could not be completed.");
        } catch (Exception e) {
            getServletContext().log("Unexpected internship API failure.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The request could not be completed.");
        }
    }

    private void deliverPendingNotifications() {
        List<PendingEmail> pending = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE TOP (20) application_email_outbox "
                             + "SET claimed_until_utc = DATEADD(MINUTE, 2, SYSUTCDATETIME()), "
                             + "attempt_count = attempt_count + 1 "
                             + "OUTPUT inserted.id, inserted.recipient_email, inserted.program_name "
                             + "WHERE sent_at_utc IS NULL AND "
                             + "(claimed_until_utc IS NULL OR claimed_until_utc <= SYSUTCDATETIME())");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                pending.add(new PendingEmail(result.getLong("id"),
                        result.getString("recipient_email"), result.getString("program_name")));
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to read pending application confirmation emails.", e);
            return;
        }

        for (PendingEmail email : pending) {
            try {
                sendEmail(email.recipient, "Internship application submitted",
                        "Your application for \"" + email.programName
                                + "\" has been submitted successfully.");
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement statement = connection.prepareStatement(
                             "UPDATE application_email_outbox SET sent_at_utc = SYSUTCDATETIME() "
                                     + ", claimed_until_utc = NULL "
                                     + "WHERE id = ? AND sent_at_utc IS NULL")) {
                    statement.setLong(1, email.id);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                getServletContext().log(
                        "Application confirmation email remains pending for outbox item "
                                + email.id + ".", e);
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement statement = connection.prepareStatement(
                             "UPDATE application_email_outbox "
                                     + "SET claimed_until_utc = DATEADD(SECOND, 30, SYSUTCDATETIME()) "
                                     + "WHERE id = ? AND sent_at_utc IS NULL")) {
                    statement.setLong(1, email.id);
                    statement.executeUpdate();
                } catch (SQLException retryUpdateError) {
                    getServletContext().log(
                            "Unable to reschedule application email outbox item "
                                    + email.id + ".", retryUpdateError);
                }
            }
        }
    }

    private void route(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String path = request.getPathInfo();
        String method = request.getMethod();
        if ("/auth/register".equals(path) && "POST".equals(method)) {
            register(request, response);
        } else if ("/auth/verify".equals(path) && "POST".equals(method)) {
            verifyEmail(request, response);
        } else if ("/auth/login".equals(path) && "POST".equals(method)) {
            login(request, response);
        } else if ("/auth/logout".equals(path) && "POST".equals(method)) {
            logout(request, response);
        } else if ("/auth/verification/resend".equals(path) && "POST".equals(method)) {
            resendVerification(request, response);
        } else if ("/programs".equals(path) && "GET".equals(method)) {
            listOpenPrograms(request, response);
        } else if (path != null && path.matches("/applications/\\d+")) {
            application(request, response, Long.parseLong(path.substring(
                    "/applications/".length())));
        } else if (path != null && path.matches("/applications/\\d+/submit")
                && "POST".equals(method)) {
            submitApplication(request, response, Long.parseLong(path.split("/")[2]));
        } else if (path != null && path.matches("/applications/\\d+/documents/[A-Za-z0-9_-]+")) {
            applicationDocument(request, response, path.split("/"));
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
        }
    }

    private static final class PendingEmail {
        private final long id;
        private final String recipient;
        private final String programName;

        private PendingEmail(long id, String recipient, String programName) {
            this.id = id;
            this.recipient = recipient;
            this.programName = programName;
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, GeneralSecurityException, IOException {
        String fullName = required(request, "fullName", 160);
        String email = required(request, "email", 254).toLowerCase(Locale.ROOT);
        String password = required(request, "password", 128);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(400, "Enter a valid email address.");
        }
        if (password.length() < 12 || !password.matches("(?s).*[A-Za-z].*")
                || !password.matches("(?s).*\\d.*")) {
            throw new ApiException(400,
                    "Password must be at least 12 characters and include a letter and a number.");
        }

        String token = newToken();
        String passwordHash = PasswordHasher.hash(password.toCharArray());
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO accounts (full_name, email, password_hash, "
                            + "verification_token_hash, verification_expires_at_utc, "
                            + "verification_window_started_at_utc, verification_sends_in_window) "
                            + "VALUES (?, ?, ?, ?, DATEADD(MINUTE, ?, SYSUTCDATETIME()), "
                            + "SYSUTCDATETIME(), 1)")) {
                statement.setString(1, fullName);
                statement.setString(2, email);
                statement.setString(3, passwordHash);
                statement.setString(4, tokenHash(token));
                statement.setInt(5, VERIFICATION_EXPIRY_MINUTES);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                if (isUniqueViolation(e)) {
                    throw new ApiException(409,
                            "This email is already registered. Please log in or request a new verification email.");
                }
                throw e;
            }
        }
        sendVerification(email, fullName, token);
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write("{\"message\":\"Account created. Verify your email before logging in.\"}");
    }

    private void verifyEmail(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String token = required(request, "token", 200);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE accounts SET email_verified = 1, verification_token_hash = NULL, "
                             + "verification_expires_at_utc = NULL "
                             + "WHERE verification_token_hash = ? AND email_verified = 0 "
                             + "AND verification_expires_at_utc > SYSUTCDATETIME()")) {
            statement.setString(1, tokenHash(token));
            if (statement.executeUpdate() != 1) {
                throw new ApiException(400, "Verification link is invalid, expired, or already used.");
            }
        }
        response.getWriter().write("{\"message\":\"Email verified. You can now log in.\"}");
    }

    private void resendVerification(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String email = required(request, "email", 254).toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(400, "Enter a valid email address.");
        }
        String token = newToken();
        String fullName;
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE accounts WITH (UPDLOCK, ROWLOCK) SET "
                            + "verification_window_started_at_utc = CASE "
                            + "WHEN verification_window_started_at_utc IS NULL OR "
                            + "DATEADD(HOUR, ?, verification_window_started_at_utc) <= SYSUTCDATETIME() "
                            + "THEN SYSUTCDATETIME() ELSE verification_window_started_at_utc END, "
                            + "verification_sends_in_window = CASE "
                            + "WHEN verification_window_started_at_utc IS NULL OR "
                            + "DATEADD(HOUR, ?, verification_window_started_at_utc) <= SYSUTCDATETIME() "
                            + "THEN 1 ELSE verification_sends_in_window + 1 END, "
                            + "verification_token_hash = ?, "
                            + "verification_expires_at_utc = DATEADD(MINUTE, ?, SYSUTCDATETIME()) "
                            + "OUTPUT inserted.full_name "
                            + "WHERE email = ? AND email_verified = 0 "
                            + "AND (verification_sends_in_window < ? OR "
                            + "verification_window_started_at_utc IS NULL OR "
                            + "DATEADD(HOUR, ?, verification_window_started_at_utc) <= SYSUTCDATETIME())")) {
                statement.setInt(1, VERIFICATION_WINDOW_HOURS);
                statement.setInt(2, VERIFICATION_WINDOW_HOURS);
                statement.setString(3, tokenHash(token));
                statement.setInt(4, VERIFICATION_EXPIRY_MINUTES);
                statement.setString(5, email);
                statement.setInt(6, MAX_VERIFICATION_EMAILS_PER_WINDOW);
                statement.setInt(7, VERIFICATION_WINDOW_HOURS);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        connection.rollback();
                        throw new ApiException(429,
                                "Verification email cannot be sent. Check the email, account status, or try again later.");
                    }
                    fullName = result.getString(1);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
        sendVerification(email, fullName, token);
        response.getWriter().write("{\"message\":\"A verification email has been sent.\"}");
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, GeneralSecurityException, IOException {
        String email = required(request, "email", 254).toLowerCase(Locale.ROOT);
        String password = required(request, "password", 128);
        long accountId;
        String storedHash;
        boolean verified;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, password_hash, email_verified FROM accounts WHERE email = ?")) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new ApiException(401, "Email or password is incorrect.");
                }
                accountId = result.getLong("id");
                storedHash = result.getString("password_hash");
                verified = result.getBoolean("email_verified");
            }
        }
        if (!PasswordHasher.verify(password.toCharArray(), storedHash)) {
            throw new ApiException(401, "Email or password is incorrect.");
        }
        if (!verified) {
            throw new ApiException(403, "Verify your email before logging in.");
        }
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute("accountId", accountId);
        response.getWriter().write("{\"message\":\"Login successful.\"}");
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.getWriter().write("{\"message\":\"Logged out.\"}");
    }

    private void listOpenPrograms(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        requireAccount(request);
        StringBuilder json = new StringBuilder("{\"programs\":[");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, name FROM internship_programs "
                             + "WHERE opens_at_utc <= SYSUTCDATETIME() "
                             + "AND SYSUTCDATETIME() < closes_at_utc ORDER BY id");
             ResultSet result = statement.executeQuery()) {
            boolean first = true;
            while (result.next()) {
                if (!first) {
                    json.append(',');
                }
                first = false;
                json.append("{\"id\":").append(result.getLong("id"))
                        .append(",\"name\":").append(jsonString(result.getString("name")))
                        .append('}');
            }
        }
        response.getWriter().write(json.append("]}").toString());
    }

    private void application(HttpServletRequest request, HttpServletResponse response, long programId)
            throws SQLException, IOException {
        long accountId = requireAccount(request);
        if ("POST".equals(request.getMethod())) {
            long applicationId = findOrCreateDraft(accountId, programId);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"id\":" + applicationId + ",\"status\":"
                    + jsonString(applicationStatus(accountId, programId)) + "}");
        } else if ("GET".equals(request.getMethod())) {
            response.getWriter().write(readApplication(accountId, programId));
        } else if ("PUT".equals(request.getMethod())) {
            saveDraft(request, response, accountId, programId);
        } else {
            sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed.");
        }
    }

    private long findOrCreateDraft(long accountId, long programId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean open;
                try (PreparedStatement program = connection.prepareStatement(
                        "SELECT id FROM internship_programs WITH (UPDLOCK, HOLDLOCK) "
                                + "WHERE id = ? AND opens_at_utc <= SYSUTCDATETIME() "
                                + "AND SYSUTCDATETIME() < closes_at_utc")) {
                    program.setLong(1, programId);
                    try (ResultSet result = program.executeQuery()) {
                        open = result.next();
                    }
                }
                if (!open) {
                    throw new ApiException(409, "This program is not currently accepting applications.");
                }

                long existing = findApplicationId(connection, accountId, programId);
                if (existing > 0) {
                    connection.commit();
                    return existing;
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO internship_applications (account_id, program_id, status) "
                                + "VALUES (?, ?, 'DRAFT')", Statement.RETURN_GENERATED_KEYS)) {
                    insert.setLong(1, accountId);
                    insert.setLong(2, programId);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Application insert did not return an ID.");
                        }
                        long id = keys.getLong(1);
                        connection.commit();
                        return id;
                    }
                }
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                if (e instanceof SQLException && isUniqueViolation((SQLException) e)) {
                    long existing = findApplicationId(connection, accountId, programId);
                    if (existing > 0) {
                        connection.commit();
                        return existing;
                    }
                }
                throw e;
            }
        }
    }

    private void saveDraft(HttpServletRequest request, HttpServletResponse response,
                           long accountId, long programId) throws SQLException, IOException {
        String phone = optional(request, "phone", 30);
        String school = optional(request, "school", 200);
        String major = optional(request, "major", 160);
        String profileText = optional(request, "profileText", 2000);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE internship_applications SET phone = ?, school = ?, major = ?, "
                             + "profile_text = ? WHERE account_id = ? AND program_id = ? AND status = 'DRAFT'")) {
            statement.setString(1, phone);
            statement.setString(2, school);
            statement.setString(3, major);
            statement.setString(4, profileText);
            statement.setLong(5, accountId);
            statement.setLong(6, programId);
            if (statement.executeUpdate() != 1) {
                ensureOwnedApplication(accountId, programId);
                throw new ApiException(409, "The application is submitted and can no longer be edited.");
            }
        }
        response.getWriter().write("{\"message\":\"Draft saved.\"}");
    }

    private String readApplication(long accountId, long programId) throws SQLException {
        StringBuilder json = new StringBuilder();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT a.id, a.status, a.phone, a.school, a.major, a.profile_text, "
                             + "d.document_type, d.file_name, r.display_name, r.is_required "
                             + "FROM internship_applications a "
                             + "LEFT JOIN application_documents d ON d.application_id = a.id "
                             + "LEFT JOIN program_document_requirements r "
                             + "ON r.program_id = a.program_id "
                             + "AND r.document_type = d.document_type "
                             + "WHERE a.account_id = ? AND a.program_id = ?")) {
            statement.setLong(1, accountId);
            statement.setLong(2, programId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new ApiException(404, "Application not found.");
                }
                json.append("{\"id\":").append(result.getLong("id"))
                        .append(",\"status\":").append(jsonString(result.getString("status")))
                        .append(",\"phone\":").append(jsonString(result.getString("phone")))
                        .append(",\"school\":").append(jsonString(result.getString("school")))
                        .append(",\"major\":").append(jsonString(result.getString("major")))
                        .append(",\"profileText\":").append(jsonString(result.getString("profile_text")))
                        .append(",\"documents\":[");
                boolean first = true;
                do {
                    String type = result.getString("document_type");
                    if (type != null) {
                        if (!first) {
                            json.append(',');
                        }
                        first = false;
                        json.append("{\"type\":").append(jsonString(type))
                                .append(",\"fileName\":").append(jsonString(result.getString("file_name")))
                                .append('}');
                    }
                } while (result.next());
                json.append("],\"requirements\":[");
            }
        }
        appendRequirements(json, programId);
        return json.append("]}").toString();
    }

    private void appendRequirements(StringBuilder json, long programId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT document_type, display_name, is_required "
                             + "FROM program_document_requirements "
                             + "WHERE program_id = ? AND document_type <> 'CV' ORDER BY id")) {
            statement.setLong(1, programId);
            try (ResultSet result = statement.executeQuery()) {
                json.append("{\"type\":\"CV\",\"name\":\"CV\",\"required\":true}");
                boolean first = false;
                while (result.next()) {
                    if (!first) {
                        json.append(',');
                    }
                    first = false;
                    json.append("{\"type\":").append(jsonString(result.getString("document_type")))
                            .append(",\"name\":").append(jsonString(result.getString("display_name")))
                            .append(",\"required\":").append(result.getBoolean("is_required"))
                            .append('}');
                }
            }
        }
    }

    private void applicationDocument(HttpServletRequest request, HttpServletResponse response,
                                     String[] pathParts) throws Exception {
        long accountId = requireAccount(request);
        long programId = Long.parseLong(pathParts[2]);
        String documentType = pathParts[4].toUpperCase(Locale.ROOT);
        if ("PUT".equals(request.getMethod())) {
            uploadDocument(request, response, accountId, programId, documentType);
        } else if ("GET".equals(request.getMethod())) {
            downloadDocument(response, accountId, programId, documentType);
        } else {
            sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed.");
        }
    }

    private void uploadDocument(HttpServletRequest request, HttpServletResponse response,
                                long accountId, long programId, String documentType)
            throws Exception {
        if (!documentType.matches("[A-Z0-9_-]{1,40}")) {
            throw new ApiException(400, "Invalid document type.");
        }
        Part part = request.getPart("file");
        if (part == null || part.getSize() == 0) {
            throw new ApiException(400, "Select a file to upload.");
        }
        boolean cv = "CV".equals(documentType);
        long maxBytes = cv ? MAX_CV_BYTES : MAX_SUPPORTING_DOCUMENT_BYTES;
        if (part.getSize() > maxBytes) {
            throw new ApiException(413, "File exceeds the size limit.");
        }
        String fileName = safeFileName(part.getSubmittedFileName());
        String extension = extension(fileName);
        boolean allowed = cv
                ? List.of("pdf", "doc", "docx").contains(extension)
                : List.of("pdf", "jpg", "jpeg", "png").contains(extension);
        if (!allowed) {
            throw new ApiException(400, cv
                    ? "CV must be a PDF, DOC, or DOCX file."
                    : "Supporting documents must be PDF or image files.");
        }
        String contentType = cv
                ? (extension.equals("pdf") ? "application/pdf" : "application/msword")
                : part.getContentType();
        if ("docx".equals(extension)) {
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            contentType = "image/jpeg";
        } else if ("png".equals(extension)) {
            contentType = "image/png";
        }
        byte[] bytes;
        try (InputStream input = part.getInputStream()) {
            bytes = input.readNBytes((int) maxBytes + 1);
        }
        if (bytes.length > maxBytes) {
            throw new ApiException(413, "File exceeds the size limit.");
        }
        if (!hasValidFileSignature(extension, bytes)) {
            throw new ApiException(400, "The file contents do not match the selected file type.");
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long applicationId = findApplicationId(connection, accountId, programId);
                if (applicationId == 0) {
                    throw new ApiException(404, "Draft application not found.");
                }
                ensureDraft(connection, applicationId);
                if (!cv && !requirementExists(connection, programId, documentType)) {
                    throw new ApiException(400, "This document is not requested for the program.");
                }
                try (PreparedStatement upsert = connection.prepareStatement(
                        "UPDATE application_documents SET file_name = ?, content_type = ?, "
                                + "file_bytes = ?, uploaded_at_utc = SYSUTCDATETIME() "
                                + "WHERE application_id = ? AND document_type = ?")) {
                    upsert.setString(1, fileName);
                    upsert.setString(2, contentType);
                    upsert.setBytes(3, bytes);
                    upsert.setLong(4, applicationId);
                    upsert.setString(5, documentType);
                    if (upsert.executeUpdate() == 0) {
                        try (PreparedStatement insert = connection.prepareStatement(
                                "INSERT INTO application_documents "
                                        + "(application_id, document_type, file_name, content_type, file_bytes) "
                                        + "VALUES (?, ?, ?, ?, ?)")) {
                            insert.setLong(1, applicationId);
                            insert.setString(2, documentType);
                            insert.setString(3, fileName);
                            insert.setString(4, contentType);
                            insert.setBytes(5, bytes);
                            insert.executeUpdate();
                        }
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
        response.getWriter().write("{\"message\":\"Document uploaded.\"}");
    }

    private void downloadDocument(HttpServletResponse response, long accountId,
                                  long programId, String documentType)
            throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT d.file_name, d.content_type, d.file_bytes "
                             + "FROM application_documents d "
                             + "JOIN internship_applications a ON a.id = d.application_id "
                             + "WHERE a.account_id = ? AND a.program_id = ? AND d.document_type = ?")) {
            statement.setLong(1, accountId);
            statement.setLong(2, programId);
            statement.setString(3, documentType);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new ApiException(404, "Document not found.");
                }
                response.setContentType(result.getString("content_type"));
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + result.getString("file_name").replace("\"", "") + "\"");
                response.setContentLengthLong(result.getBytes("file_bytes").length);
                response.getOutputStream().write(result.getBytes("file_bytes"));
            }
        }
    }

    private void submitApplication(HttpServletRequest request, HttpServletResponse response,
                                   long programId) throws SQLException, IOException {
        long accountId = requireAccount(request);
        Submission submission = submit(accountId, programId);
        deliverPendingNotifications();
        boolean notificationSent = notificationSent(submission.applicationId);
        response.getWriter().write(notificationSent
                ? "{\"status\":\"SUBMITTED\",\"notification\":\"sent\"}"
                : "{\"status\":\"SUBMITTED\",\"notification\":\"pending\",\"message\":\"Application submitted; confirmation email delivery is pending.\"}");
    }

    private Submission submit(long accountId, long programId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean open;
                try (PreparedStatement program = connection.prepareStatement(
                        "SELECT id FROM internship_programs WITH (UPDLOCK, HOLDLOCK) "
                                + "WHERE id = ? AND opens_at_utc <= SYSUTCDATETIME() "
                                + "AND SYSUTCDATETIME() < closes_at_utc")) {
                    program.setLong(1, programId);
                    try (ResultSet result = program.executeQuery()) {
                        open = result.next();
                    }
                }
                if (!open) {
                    throw new ApiException(409, "This program is closed and cannot accept applications.");
                }

                long applicationId = findApplicationId(connection, accountId, programId);
                if (applicationId == 0) {
                    throw new ApiException(404, "Draft application not found.");
                }
                try (PreparedStatement fields = connection.prepareStatement(
                        "SELECT a.status, a.phone, a.school, a.major, c.email "
                                + "FROM internship_applications a WITH (UPDLOCK, HOLDLOCK) "
                                + "JOIN accounts c ON c.id = a.account_id "
                                + "WHERE a.id = ? AND c.email_verified = 1 "
                                + "AND a.account_id = ?")) {
                    fields.setLong(1, applicationId);
                    fields.setLong(2, accountId);
                    try (ResultSet result = fields.executeQuery()) {
                        if (!result.next()) {
                            throw new ApiException(403, "Verify your email before submitting.");
                        }
                        if (!"DRAFT".equals(result.getString("status"))) {
                            throw new ApiException(409, "This application has already been submitted.");
                        }
                        List<String> missing = new ArrayList<>();
                        addMissing(missing, "phone", result.getString("phone"));
                        addMissing(missing, "school", result.getString("school"));
                        addMissing(missing, "major", result.getString("major"));
                        if (!missing.isEmpty()) {
                            throw new ApiException(400,
                                    "Complete the required fields: " + String.join(", ", missing) + ".");
                        }
                    }
                }

                List<String> missingDocuments = missingRequiredDocuments(
                        connection, applicationId, programId);
                if (!missingDocuments.isEmpty()) {
                    throw new ApiException(400,
                            "Upload the required documents: " + String.join(", ", missingDocuments) + ".");
                }
                String email;
                String programName;
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE internship_applications SET status = 'SUBMITTED', "
                                + "submitted_at_utc = SYSUTCDATETIME() "
                                + "WHERE id = ? AND status = 'DRAFT'")) {
                    update.setLong(1, applicationId);
                    if (update.executeUpdate() != 1) {
                        throw new ApiException(409, "This application has already been submitted.");
                    }
                }
                try (PreparedStatement details = connection.prepareStatement(
                        "SELECT c.email, p.name FROM accounts c "
                                + "JOIN internship_applications a ON a.account_id = c.id "
                                + "JOIN internship_programs p ON p.id = a.program_id "
                                + "WHERE a.id = ?")) {
                    details.setLong(1, applicationId);
                    try (ResultSet result = details.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Submitted application details were not found.");
                        }
                        email = result.getString("email");
                        programName = result.getString("name");
                    }
                }
                try (PreparedStatement outbox = connection.prepareStatement(
                        "INSERT INTO application_email_outbox "
                                + "(application_id, recipient_email, program_name) VALUES (?, ?, ?)")) {
                    outbox.setLong(1, applicationId);
                    outbox.setString(2, email);
                    outbox.setString(3, programName);
                    outbox.executeUpdate();
                }
                connection.commit();
                return new Submission(applicationId);
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private List<String> missingRequiredDocuments(Connection connection, long applicationId,
                                                   long programId) throws SQLException {
        List<String> missing = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT r.document_type, r.display_name "
                        + "FROM program_document_requirements r WITH (UPDLOCK, HOLDLOCK) "
                        + "LEFT JOIN application_documents d ON d.application_id = ? "
                        + "AND d.document_type = r.document_type "
                        + "WHERE r.program_id = ? AND r.is_required = 1 "
                        + "AND r.document_type <> 'CV' AND d.id IS NULL")) {
            statement.setLong(1, applicationId);
            statement.setLong(2, programId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    missing.add(result.getString("display_name"));
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM application_documents "
                        + "WHERE application_id = ? AND document_type = 'CV'")) {
            statement.setLong(1, applicationId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    missing.add("CV");
                }
            }
        }
        return missing;
    }

    private void sendVerification(String email, String fullName, String token) throws SQLException {
        String url = verificationUrlBase + (verificationUrlBase.contains("?") ? "&" : "?")
                + "token=" + token;
        sendEmail(email, "Verify your internship account",
                "Hello " + fullName + ",\n\nVerify your email using this link:\n" + url
                        + "\n\nThis link expires in " + VERIFICATION_EXPIRY_MINUTES + " minutes.");
    }

    private boolean notificationSent(long applicationId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT sent_at_utc FROM application_email_outbox WHERE application_id = ?")) {
            statement.setLong(1, applicationId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Application email notification was not queued.");
                }
                return result.getTimestamp(1) != null;
            }
        }
    }

    private void sendEmail(String recipient, String subject, String body) throws SQLException {
        try {
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(verificationFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            message.setText(body, StandardCharsets.UTF_8.name());
            Transport.send(message);
        } catch (Exception e) {
            throw new SQLException("Email delivery failed.", e);
        }
    }

    private long requireAccount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("accountId") instanceof Long)) {
            throw new ApiException(401, "Log in to continue.");
        }
        return (Long) session.getAttribute("accountId");
    }

    private long findApplicationId(Connection connection, long accountId, long programId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM internship_applications WHERE account_id = ? AND program_id = ?")) {
            statement.setLong(1, accountId);
            statement.setLong(2, programId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : 0;
            }
        }
    }

    private void ensureOwnedApplication(long accountId, long programId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (findApplicationId(connection, accountId, programId) == 0) {
                throw new ApiException(404, "Application not found.");
            }
        }
    }

    private void ensureDraft(Connection connection, long applicationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT status FROM internship_applications WITH (UPDLOCK, HOLDLOCK) WHERE id = ?")) {
            statement.setLong(1, applicationId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new ApiException(404, "Application not found.");
                }
                if (!"DRAFT".equals(result.getString(1))) {
                    throw new ApiException(409, "The application is submitted and can no longer be edited.");
                }
            }
        }
    }

    private boolean requirementExists(Connection connection, long programId, String documentType)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM program_document_requirements "
                        + "WHERE program_id = ? AND document_type = ?")) {
            statement.setLong(1, programId);
            statement.setString(2, documentType);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static String required(HttpServletRequest request, String name, int maxLength) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new ApiException(400, "A valid " + name + " is required.");
        }
        return value.trim();
    }

    private static String optional(HttpServletRequest request, String name, int maxLength) {
        String value = request.getParameter(name);
        if (value != null && value.length() > maxLength) {
            throw new ApiException(400, name + " is too long.");
        }
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String tokenHash(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                hex.append(String.format("%02x", value & 0xff));
            }
            return hex.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 is unavailable.", e);
        }
    }

    private static String safeFileName(String submittedName) {
        if (submittedName == null) {
            throw new ApiException(400, "File name is missing.");
        }
        String name = submittedName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\r\\n\"\\\\]", "_");
        if (name.isBlank() || name.length() > 255) {
            throw new ApiException(400, "File name is invalid.");
        }
        return name;
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean hasValidFileSignature(String extension, byte[] bytes) {
        if (extension.equals("pdf")) {
            return bytes.length >= 5 && bytes[0] == '%'
                    && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-';
        }
        if (extension.equals("png")) {
            byte[] signature = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};
            return startsWith(bytes, signature);
        }
        if (extension.equals("jpg") || extension.equals("jpeg")) {
            return bytes.length >= 3 && (bytes[0] & 0xff) == 0xff
                    && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        }
        if (extension.equals("doc")) {
            byte[] signature = {(byte) 0xd0, (byte) 0xcf, 0x11, (byte) 0xe0,
                    (byte) 0xa1, (byte) 0xb1, 0x1a, (byte) 0xe1};
            return startsWith(bytes, signature);
        }
        return extension.equals("docx") && bytes.length >= 4
                && bytes[0] == 'P' && bytes[1] == 'K'
                && bytes[2] == 3 && bytes[3] == 4;
    }

    private static boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static void addMissing(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }

    private static boolean isUniqueViolation(SQLException e) {
        for (SQLException current = e; current != null; current = current.getNextException()) {
            if (current.getErrorCode() == 2601 || current.getErrorCode() == 2627) {
                return true;
            }
        }
        return false;
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
            }
        }
        return escaped.append('"').toString();
    }

    private static void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status);
        response.getWriter().write("{\"error\":" + jsonString(message) + "}");
    }

    private static final class Submission {
        private final long applicationId;

        private Submission(long applicationId) {
            this.applicationId = applicationId;
        }
    }

    private static final class ApiException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final int status;

        private ApiException(int status, String message) {
            super(message);
            this.status = status;
        }
    }
}
