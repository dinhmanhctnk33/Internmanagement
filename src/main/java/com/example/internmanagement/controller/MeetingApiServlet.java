package com.example.internmanagement.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.example.internmanagement.dao.MeetingDAO;
import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/meetings")
public class MeetingApiServlet extends HttpServlet {
    private final MeetingDAO meetingDAO = new MeetingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        User user = authenticatedUser(request, "INTERN");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            long meetingId = parseId(request.getParameter("id"));
            try (Connection connection = DBConnection.getConnection()) {
                MeetingDAO.MeetingDetails meeting = meetingDAO.findForAttendee(connection, meetingId, user.getId());
                if (meeting == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.getWriter().write("{\"id\":" + meeting.id()
                        + ",\"title\":" + jsonString(meeting.title())
                        + ",\"description\":" + jsonString(meeting.description())
                        + ",\"startsAt\":" + jsonString(meeting.startsAt().toString()) + "}");
            }
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã cuộc họp không hợp lệ.");
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải cuộc họp.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        User mentor = authenticatedUser(request, "MENTOR");
        if (mentor == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String startsAtValue = request.getParameter("startsAt");
        String[] attendeeValues = request.getParameterValues("internProfileIds");
        if (attendeeValues == null) {
            attendeeValues = request.getParameterValues("internIds");
        }
        if (startsAtValue == null || startsAtValue.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cần cung cấp thời gian bắt đầu cuộc họp.");
            return;
        }

        try {
            LocalDateTime startsAt = LocalDateTime.parse(startsAtValue);
            List<Integer> internProfileIds = parseInternProfileIds(attendeeValues);
            try (Connection connection = DBConnection.getConnection()) {
                long meetingId = meetingDAO.createForMentor(connection, mentor.getId(), title, description,
                        startsAt, internProfileIds);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"success\":true,\"meetingId\":" + meetingId + "}");
            }
        } catch (DateTimeParseException | NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thời gian hoặc danh sách người dự không hợp lệ.");
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (SQLException exception) {
            throw new ServletException("Không thể tạo cuộc họp.", exception);
        }
    }

    private User authenticatedUser(HttpServletRequest request, String requiredRole) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("currentUser");
        if (!(value instanceof User)) {
            value = session.getAttribute("user");
        }
        if (!(value instanceof User user)) {
            return null;
        }
        Object sessionRole = session.getAttribute("userRole");
        String role = sessionRole instanceof String valueRole ? valueRole : user.getRoleCode();
        return user.getId() > 0 && requiredRole.equalsIgnoreCase(role) ? user : null;
    }

    private long parseId(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing meeting id");
        }
        long id = Long.parseLong(value);
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid meeting id");
        }
        return id;
    }

    private List<Integer> parseInternProfileIds(String[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Cần mời ít nhất một thực tập sinh.");
        }
        List<Integer> ids = new ArrayList<>();
        for (String value : values) {
            int id = Integer.parseInt(value);
            if (id <= 0 || ids.contains(id)) {
                throw new IllegalArgumentException("Danh sách thực tập sinh không hợp lệ.");
            }
            ids.add(id);
        }
        return ids;
    }

    private String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder result = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (character < 0x20) {
                        result.append(String.format("\\u%04x", (int) character));
                    } else {
                        result.append(character);
                    }
                }
            }
        }
        return result.append('"').toString();
    }
}