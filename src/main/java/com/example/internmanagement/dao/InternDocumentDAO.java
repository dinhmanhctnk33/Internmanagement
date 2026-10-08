package com.example.internmanagement.dao;

import com.example.internmanagement.model.InternDocument;
import com.example.internmanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InternDocumentDAO {

    /**
     * Lấy danh sách tài liệu theo ID thực tập sinh
     */
    public List<InternDocument> findByIntern(int internId) throws SQLException {
        List<InternDocument> list = new ArrayList<>();
        String sql = "SELECT * FROM intern_documents WHERE intern_profile_id = ? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, internId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDocument(rs));
                }
            }
        }
        return list;
    }

    public InternDocument findByFileUrl(String fileUrl) throws SQLException {
        String sql = "SELECT * FROM intern_documents WHERE file_url = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fileUrl);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToDocument(rs) : null;
            }
        }
    }

    /**
     * Thêm mới một tài liệu (Thực tập sinh upload)
     */
    public void add(InternDocument document) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            add(conn, document);
        }
    }

    public int add(Connection conn, InternDocument document) throws SQLException {
        String sql = "INSERT INTO intern_documents " +
                     "(intern_profile_id, document_type, file_name, file_url, file_size_bytes, approval_status, uploaded_at) " +
                     "VALUES (?, ?, ?, ?, ?, 'PENDING', ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, document.getInternProfileId());
            ps.setString(2, document.getDocumentType());
            ps.setString(3, document.getFileName());
            ps.setString(4, document.getFileUrl());

            if (document.getFileSizeBytes() != null) {
                ps.setLong(5, document.getFileSizeBytes());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }

            LocalDateTime uploadedAt = document.getUploadedAt() != null ? document.getUploadedAt() : LocalDateTime.now();
            ps.setTimestamp(6, Timestamp.valueOf(uploadedAt));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("Không lấy được ID tài liệu sau khi thêm.");
        }
    }

    /**
     * Cập nhật trạng thái duyệt tài liệu (Quản lý/Mentor duyệt)
     */
    public void review(int documentId, String approvalStatus, String rejectionNote, Long reviewerId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            review(conn, documentId, approvalStatus, rejectionNote, reviewerId);
        }
    }

    public boolean review(Connection conn, int documentId, String approvalStatus,
                          String rejectionNote, Long reviewerId) throws SQLException {
        String sql = "UPDATE intern_documents " +
                     "SET approval_status = ?, rejection_note = ? " +
                     "WHERE id = ? AND approval_status = 'PENDING'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, approvalStatus);
            ps.setString(2, rejectionNote);
            ps.setInt(3, documentId);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Ánh xạ kết quả ResultSet thành đối tượng InternDocument
     */
    private InternDocument mapResultSetToDocument(ResultSet rs) throws SQLException {
        InternDocument document = new InternDocument();

        document.setId(rs.getInt("id"));
        document.setInternProfileId(rs.getInt("intern_profile_id"));
        document.setDocumentType(rs.getString("document_type"));
        document.setFileName(rs.getString("file_name"));
        document.setFileUrl(rs.getString("file_url"));
        document.setFileSizeBytes(rs.getObject("file_size_bytes") != null ? rs.getLong("file_size_bytes") : null);
        document.setApprovalStatus(rs.getString("approval_status"));
        document.setRejectionNote(rs.getString("rejection_note"));

        Timestamp uploadedAtTs = rs.getTimestamp("uploaded_at");
        if (uploadedAtTs != null) {
            document.setUploadedAt(uploadedAtTs.toLocalDateTime());
        }

        return document;
    }
}
