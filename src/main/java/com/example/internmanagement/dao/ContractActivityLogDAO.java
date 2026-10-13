package com.example.internmanagement.dao;

import com.example.internmanagement.model.ContractActivityLog;
import com.example.internmanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO ghi nhật ký hoạt động hợp đồng. Bảng contract_activity_logs KHÔNG có
 * ràng buộc ON DELETE CASCADE từ contracts, để đúng yêu cầu "khi HR xóa hợp
 * đồng, nhật ký giữ lại thông tin xác nhận đã có".
 */
public class ContractActivityLogDAO {

    public Long insert(ContractActivityLog log) {
        String sql = "INSERT INTO contract_activity_logs " +
                "(contract_id, action_type, actor_user_id, actor_source, file_version, reason, " +
                " old_value_json, new_value_json, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, log.getContractId());
            ps.setString(2, log.getActionType());
            if (log.getActorUserId() != null) ps.setLong(3, log.getActorUserId()); else ps.setNull(3, Types.BIGINT);
            ps.setString(4, log.getActorSource());
            if (log.getFileVersion() != null) ps.setInt(5, log.getFileVersion()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, log.getReason());
            ps.setString(7, log.getOldValueJson());
            ps.setString(8, log.getNewValueJson());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ContractActivityLog> findByContractId(Long contractId) {
        String sql = "SELECT * FROM contract_activity_logs WHERE contract_id = ? ORDER BY created_at ASC";
        List<ContractActivityLog> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ContractActivityLog log = new ContractActivityLog();
                    log.setId(rs.getLong("id"));
                    log.setContractId(rs.getLong("contract_id"));
                    log.setActionType(rs.getString("action_type"));
                    long actorId = rs.getLong("actor_user_id");
                    log.setActorUserId(rs.wasNull() ? null : actorId);
                    log.setActorSource(rs.getString("actor_source"));
                    int fv = rs.getInt("file_version");
                    log.setFileVersion(rs.wasNull() ? null : fv);
                    log.setReason(rs.getString("reason"));
                    log.setOldValueJson(rs.getString("old_value_json"));
                    log.setNewValueJson(rs.getString("new_value_json"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    result.add(log);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
