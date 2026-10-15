package com.example.internmanagement.dao;

import com.example.internmanagement.model.WorkSchedule;
import com.example.internmanagement.util.DBConnection; // Hoặc class DBUtil trong gói util của bạn

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkScheduleDAO {

    // Lấy danh sách tất cả lịch làm việc của các nhóm
    public List<WorkSchedule> getAllSchedules() {
        List<WorkSchedule> list = new ArrayList<>();
        String sql = "SELECT ws.*, g.name AS group_name FROM work_schedules ws " +
                     "LEFT JOIN groups g ON ws.group_id = g.id ORDER BY ws.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                WorkSchedule schedule = new WorkSchedule(
                    rs.getInt("id"),
                    rs.getInt("group_id"),
                    rs.getString("group_name"),
                    rs.getString("shift_name"),
                    rs.getString("start_time"),
                    rs.getString("end_time"),
                    rs.getString("work_days"),
                    rs.getString("note")
                );
                list.add(schedule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy lịch làm việc theo ID
    public WorkSchedule getScheduleById(int id) {
        String sql = "SELECT ws.*, g.name AS group_name FROM work_schedules ws " +
                     "LEFT JOIN groups g ON ws.group_id = g.id WHERE ws.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new WorkSchedule(
                        rs.getInt("id"),
                        rs.getInt("group_id"),
                        rs.getString("group_name"),
                        rs.getString("shift_name"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("work_days"),
                        rs.getString("note")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Thêm mới thiết lập lịch làm việc cho nhóm
    public boolean insertSchedule(WorkSchedule schedule) {
        String sql = "INSERT INTO work_schedules (group_id, shift_name, start_time, end_time, work_days, note) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, schedule.getGroupId());
            ps.setString(2, schedule.getShiftName());
            ps.setString(3, schedule.getStartTime());
            ps.setString(4, schedule.getEndTime());
            ps.setString(5, schedule.getWorkDays());
            ps.setString(6, schedule.getNote());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật lịch làm việc của nhóm
    public boolean updateSchedule(WorkSchedule schedule) {
        String sql = "UPDATE work_schedules SET group_id = ?, shift_name = ?, start_time = ?, end_time = ?, work_days = ?, note = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, schedule.getGroupId());
            ps.setString(2, schedule.getShiftName());
            ps.setString(3, schedule.getStartTime());
            ps.setString(4, schedule.getEndTime());
            ps.setString(5, schedule.getWorkDays());
            ps.setString(6, schedule.getNote());
            ps.setInt(7, schedule.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa thiết lập lịch làm việc
    public boolean deleteSchedule(int id) {
        String sql = "DELETE FROM work_schedules WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}