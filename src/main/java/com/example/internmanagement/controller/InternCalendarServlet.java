package com.example.internmanagement.controller;

import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/my-calendar")
public class InternCalendarServlet extends HttpServlet {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"INTERN".equalsIgnoreCase(String.valueOf(request.getSession().getAttribute("userRole")))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        User user = (User) request.getSession().getAttribute("user");
        YearMonth month = requestedMonth(request.getParameter("month"));
        try (Connection connection = DBConnection.getConnection()) {
            Map<String, Object> schedule = loadSchedule(connection, user.getId());
            List<Map<String, Object>> events = schedule.isEmpty()
                    ? List.of() : loadEvents(connection, schedule);
            request.setAttribute("schedule", schedule);
            request.setAttribute("events", events);
            request.setAttribute("calendarWeeks", buildCalendar(month, events));
            request.setAttribute("monthValue", month.toString());
            request.setAttribute("monthLabel", "Tháng " + month.getMonthValue() + "/" + month.getYear());
            request.setAttribute("previousMonth", month.minusMonths(1).toString());
            request.setAttribute("nextMonth", month.plusMonths(1).toString());
            request.setAttribute("currentUser", user);
            request.getRequestDispatcher("/WEB-INF/views/intern-calendar.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải lịch thực tập cá nhân.", exception);
        }
    }

    private Map<String, Object> loadSchedule(Connection connection, int userId) throws SQLException {
        String sql = "SELECT ip.id profile_id,ip.intern_code,ip.start_date profile_start,ip.end_date profile_end,"
                + "p.id program_id,p.program_code,p.program_name,p.start_date program_start,p.end_date program_end,"
                + "p.status program_status,d.dept_name,m.mentor_code,mu.full_name mentor_name,"
                + "c.contract_number,c.effective_date contract_start,c.expiration_date contract_end,c.signature_status "
                + "FROM intern_profiles ip LEFT JOIN internship_programs p ON p.id=ip.program_id "
                + "LEFT JOIN departments d ON d.id=p.department_id LEFT JOIN mentors m ON m.id=ip.mentor_id "
                + "LEFT JOIN users mu ON mu.id=m.user_id LEFT JOIN contracts c ON c.id=(SELECT MAX(c2.id) FROM contracts c2 WHERE c2.intern_profile_id=ip.id) "
                + "WHERE ip.user_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Map.of();
                Map<String, Object> row = row(result);
                Object start = row.get("contract_start") != null ? row.get("contract_start")
                        : row.get("profile_start") != null ? row.get("profile_start") : row.get("program_start");
                Object end = row.get("contract_end") != null ? row.get("contract_end")
                        : row.get("profile_end") != null ? row.get("profile_end") : row.get("program_end");
                row.put("effective_start", start);
                row.put("effective_end", end);
                return row;
            }
        }
    }

    private List<Map<String, Object>> loadEvents(Connection connection, Map<String, Object> schedule) throws SQLException {
        List<Map<String, Object>> events = new ArrayList<>();
        addEvent(events, schedule.get("effective_start"), "Bắt đầu kỳ thực tập", "start", "Mốc bắt đầu áp dụng cho lịch cá nhân");
        addEvent(events, schedule.get("effective_end"), "Kết thúc kỳ thực tập", "end", "Mốc kết thúc áp dụng cho lịch cá nhân");
        if (schedule.get("contract_start") != null) {
            addEvent(events, schedule.get("contract_start"), "Hợp đồng có hiệu lực", "contract", String.valueOf(schedule.get("contract_number")));
        }
        if (schedule.get("program_id") != null) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT milestone_title,target_date,description FROM program_milestones WHERE program_id=? AND target_date IS NOT NULL ORDER BY target_date")) {
                statement.setObject(1, schedule.get("program_id"));
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        addEvent(events, result.getDate("target_date"), result.getString("milestone_title"),
                                "milestone", result.getString("description"));
                    }
                }
            }
        }
        events.sort((first, second) -> String.valueOf(first.get("date")).compareTo(String.valueOf(second.get("date"))));
        return events;
    }

    private void addEvent(List<Map<String, Object>> events, Object date, String title, String type, String description) {
        if (date == null) return;
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("date", String.valueOf(date));
        event.put("title", title);
        event.put("type", type);
        event.put("description", description == null ? "" : description);
        events.add(event);
    }

    private List<List<Map<String, Object>>> buildCalendar(YearMonth month, List<Map<String, Object>> events) {
        Map<String, List<Map<String, Object>>> eventsByDate = new HashMap<>();
        for (Map<String, Object> event : events) {
            eventsByDate.computeIfAbsent(String.valueOf(event.get("date")), ignored -> new ArrayList<>()).add(event);
        }
        LocalDate first = month.atDay(1);
        int leading = first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        LocalDate cursor = first.minusDays(leading);
        List<List<Map<String, Object>>> weeks = new ArrayList<>();
        do {
            List<Map<String, Object>> week = new ArrayList<>();
            for (int day = 0; day < 7; day++) {
                Map<String, Object> cell = new LinkedHashMap<>();
                cell.put("date", cursor.format(ISO));
                cell.put("day", cursor.getDayOfMonth());
                cell.put("inMonth", cursor.getMonthValue() == month.getMonthValue());
                cell.put("today", cursor.equals(LocalDate.now()));
                cell.put("events", eventsByDate.getOrDefault(cursor.format(ISO), List.of()));
                week.add(cell);
                cursor = cursor.plusDays(1);
            }
            weeks.add(week);
        } while (cursor.getMonthValue() == month.getMonthValue() || weeks.size() < 5);
        return weeks;
    }

    private Map<String, Object> row(ResultSet result) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int index = 1; index <= result.getMetaData().getColumnCount(); index++) {
            row.put(result.getMetaData().getColumnLabel(index), result.getObject(index));
        }
        return row;
    }

    private YearMonth requestedMonth(String value) {
        try {
            return value == null || value.isBlank() ? YearMonth.now() : YearMonth.parse(value);
        } catch (Exception ignored) {
            return YearMonth.now();
        }
    }
}
