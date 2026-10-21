package com.example.internmanagement.dao;

import com.example.internmanagement.model.WorkSchedule;
import com.example.internmanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkScheduleDAO {
    private static final String SELECT = "SELECT ws.id, ws.group_id, d.dept_name AS group_name, "
            + "ws.shift_name, TIME_FORMAT(ws.start_time,'%H:%i') AS start_time, "
            + "TIME_FORMAT(ws.end_time,'%H:%i') AS end_time, ws.work_days, ws.note "
            + "FROM work_schedules ws JOIN departments d ON d.id=ws.group_id ";

    public List<WorkSchedule> findAll() throws SQLException {
        List<WorkSchedule> result = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY d.dept_name, ws.start_time");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    public WorkSchedule find(long id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE ws.id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Map<String, Object>> activeGroups() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, dept_code, dept_name FROM departments WHERE is_active=1 ORDER BY dept_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getLong("id"));
                row.put("code", rs.getString("dept_code"));
                row.put("name", rs.getString("dept_name"));
                result.add(row);
            }
        }
        return result;
    }

    public long save(WorkSchedule schedule) throws SQLException {
        if (schedule.getId() > 0) {
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(
                    "UPDATE work_schedules SET group_id=?,shift_name=?,start_time=?,end_time=?,work_days=?,note=? WHERE id=?")) {
                bind(ps, schedule); ps.setLong(7, schedule.getId());
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Không tìm thấy lịch làm việc cần cập nhật.");
                return schedule.getId();
            }
        }
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO work_schedules(group_id,shift_name,start_time,end_time,work_days,note) VALUES(?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, schedule); ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); return keys.getLong(1); }
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM work_schedules WHERE id=?")) {
            ps.setLong(1, id); return ps.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement ps, WorkSchedule s) throws SQLException {
        ps.setLong(1, s.getGroupId()); ps.setString(2, s.getShiftName());
        ps.setString(3, s.getStartTime()); ps.setString(4, s.getEndTime());
        ps.setString(5, s.getWorkDays()); ps.setString(6, s.getNote());
    }

    private WorkSchedule map(ResultSet rs) throws SQLException {
        return new WorkSchedule(rs.getLong("id"), rs.getLong("group_id"), rs.getString("group_name"),
                rs.getString("shift_name"), rs.getString("start_time"), rs.getString("end_time"),
                rs.getString("work_days"), rs.getString("note"));
    }
}
