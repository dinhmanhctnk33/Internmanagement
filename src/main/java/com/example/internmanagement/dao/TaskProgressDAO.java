package com.example.internmanagement.dao;

import com.example.internmanagement.model.TaskProgress;
import com.example.internmanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskProgressDAO {

    public List<TaskProgress> getProgressByInternId(int internId) {
        List<TaskProgress> list = new ArrayList<>();
        String sql = "SELECT * FROM task_progress WHERE intern_id = ? ORDER BY updated_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, internId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TaskProgress(
                        rs.getInt("id"),
                        rs.getInt("intern_id"),
                        rs.getInt("task_id"),
                        rs.getString("task_name"),
                        rs.getInt("progress_percent"),
                        rs.getString("status"),
                        rs.getString("note"),
                        rs.getTimestamp("updated_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveOrUpdateProgress(TaskProgress progress) {
        String sql = "INSERT INTO task_progress (intern_id, task_id, task_name, progress_percent, status, note) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE progress_percent = VALUES(progress_percent), " +
                     "status = VALUES(status), note = VALUES(note), updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, progress.getInternId());
            ps.setInt(2, progress.getTaskId());
            ps.setString(3, progress.getTaskName());
            ps.setInt(4, progress.getProgressPercent());
            ps.setString(5, progress.getStatus());
            ps.setString(6, progress.getNote());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}