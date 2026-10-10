package com.example.internmanagement.controller;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;

@WebServlet("/attendance")
public class InternAttendanceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = requireIntern(request, response);
        if (user == null) return;
        try (Connection connection = DBConnection.getConnection()) {
            long profileId = profileId(connection, user.getId());
            request.setAttribute("today", today(connection, profileId));
            request.setAttribute("records", history(connection, profileId));
            request.setAttribute("monthSummary", monthSummary(connection, profileId));
            request.setAttribute("leaveRequests", leaves(connection, profileId));
            request.setAttribute("currentUser", user);
            request.getRequestDispatcher("/WEB-INF/views/intern-attendance.jsp").forward(request, response);
        } catch (IllegalArgumentException exception) {
            request.setAttribute("errorMessage", exception.getMessage());
            request.setAttribute("currentUser", user);
            request.getRequestDispatcher("/WEB-INF/views/intern-attendance.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải dữ liệu chấm công.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = requireIntern(request, response);
        if (user == null) return;
        String action = request.getParameter("action");
        try (Connection connection = DBConnection.getConnection()) {
            long profileId = profileId(connection, user.getId());
            if ("check-in".equals(action)) checkIn(connection, profileId, request.getRemoteAddr());
            else if ("check-out".equals(action)) checkOut(connection, profileId);
            else throw new IllegalArgumentException("Thao tác chấm công không hợp lệ.");
            redirect(response, request, "success=" + ("check-in".equals(action) ? "checked-in" : "checked-out"));
        } catch (IllegalArgumentException exception) {
            redirect(response, request, "error=" + URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8));
        } catch (SQLException exception) {
            String message = exception.getErrorCode() == 1062
                    ? "Bạn đã check-in hôm nay." : "Không thể ghi nhận chấm công. Vui lòng thử lại.";
            redirect(response, request, "error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
        }
    }

    private void checkIn(Connection connection, long profileId, String ip) throws SQLException {
        String status=LocalTime.now().isAfter(workStart().plusMinutes(graceMinutes()))?"LATE":"ON_TIME";
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO attendance_records(intern_profile_id,work_date,check_in_time,ip_address,status) "
                        + "VALUES(?,CURDATE(),NOW(),?,?)")) {
            statement.setLong(1, profileId);
            statement.setString(2, ip);
            statement.setString(3, status);
            statement.executeUpdate();
        }
    }

    private void checkOut(Connection connection, long profileId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE attendance_records SET check_out_time=NOW(),status=CASE WHEN TIME(NOW())<? AND status='ON_TIME' THEN 'EARLY_LEAVE' ELSE status END WHERE intern_profile_id=? "
                        + "AND work_date=CURDATE() AND check_in_time IS NOT NULL AND check_out_time IS NULL")) {
            statement.setTime(1, java.sql.Time.valueOf(workEnd()));
            statement.setLong(2, profileId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("Bạn chưa check-in hoặc đã check-out hôm nay.");
            }
        }
    }

    private Map<String, Object> today(Connection connection, long profileId) throws SQLException {
        String sql = "SELECT id,work_date,TIME_FORMAT(check_in_time,'%H:%i:%s') check_in_display," 
                + "TIME_FORMAT(check_out_time,'%H:%i:%s') check_out_display,status "
                + "FROM attendance_records WHERE intern_profile_id=? AND work_date=CURDATE()";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, profileId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? row(result) : null;
            }
        }
    }

    private List<Map<String, Object>> history(Connection connection, long profileId) throws SQLException {
        String sql = "SELECT work_date,TIME_FORMAT(check_in_time,'%H:%i') check_in_display," 
                + "TIME_FORMAT(check_out_time,'%H:%i') check_out_display," 
                + "CASE WHEN check_out_time IS NULL THEN NULL ELSE TIMESTAMPDIFF(MINUTE,check_in_time,check_out_time) END total_minutes,status "
                + "FROM attendance_records WHERE intern_profile_id=? ORDER BY work_date DESC LIMIT 31";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, profileId);
            List<Map<String, Object>> rows = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) rows.add(row(result));
            }
            return rows;
        }
    }

    private Map<String, Object> monthSummary(Connection connection, long profileId) throws SQLException {
        String sql = "SELECT COUNT(*) work_days,CAST(COALESCE(SUM(CASE WHEN check_out_time IS NOT NULL "
                + "THEN TIMESTAMPDIFF(MINUTE,check_in_time,check_out_time) ELSE 0 END),0) AS SIGNED) total_minutes "
                + "FROM attendance_records WHERE intern_profile_id=? AND YEAR(work_date)=YEAR(CURDATE()) "
                + "AND MONTH(work_date)=MONTH(CURDATE())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, profileId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return row(result);
            }
        }
    }

    private List<Map<String,Object>> leaves(Connection connection,long profileId)throws SQLException{
        try(PreparedStatement statement=connection.prepareStatement("SELECT id,leave_type,start_date,end_date,total_days,reason,approval_status,approver_notes FROM leave_requests WHERE intern_profile_id=? ORDER BY id DESC LIMIT 20")){statement.setLong(1,profileId);List<Map<String,Object>> result=new ArrayList<>();try(ResultSet rs=statement.executeQuery()){while(rs.next())result.add(row(rs));}return result;}
    }

    private long profileId(Connection connection, int userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM intern_profiles WHERE user_id=? ORDER BY id DESC LIMIT 1")) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Tài khoản chưa có hồ sơ thực tập sinh.");
                return result.getLong(1);
            }
        }
    }

    private Map<String, Object> row(ResultSet result) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
            row.put(result.getMetaData().getColumnLabel(i), result.getObject(i));
        }
        return row;
    }

    private User requireIntern(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"INTERN".equalsIgnoreCase(String.valueOf(request.getSession().getAttribute("userRole")))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    private void redirect(HttpServletResponse response, HttpServletRequest request, String query) throws IOException {
        response.sendRedirect(request.getContextPath() + "/attendance?" + query);
    }

    private LocalTime workStart(){return parseTime("IMS_WORK_START","09:00");}
    private LocalTime workEnd(){return parseTime("IMS_WORK_END","17:00");}
    private int graceMinutes(){try{return Integer.parseInt(value("IMS_WORK_GRACE","0"));}catch(Exception ignored){return 0;}}
    private LocalTime parseTime(String key,String fallback){try{return LocalTime.parse(value(key,fallback));}catch(Exception ignored){return LocalTime.parse(fallback);}}
    private String value(String key,String fallback){String result=System.getenv(key);if(result==null||result.isBlank())result=System.getProperty(key);return result==null||result.isBlank()?fallback:result.trim();}
}
