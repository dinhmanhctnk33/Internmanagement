package com.example.internmanagement.dao;

import com.example.internmanagement.model.Contract;
import com.example.internmanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO cho hợp đồng, tập trung xử lý đúng các quy tắc nghiệp vụ của
 * "Thực tập sinh xác nhận hợp đồng":
 *   - Xác nhận chạy lặp / bấm 2 lần / mở 2 tab KHÔNG tạo kết quả trùng.
 *   - Xác nhận gắn đúng với file đang hiển thị (so khớp fileVersion).
 *   - HR đặt "Đã xác nhận" thủ công cũng đi qua cùng 1 cơ chế an toàn.
 *
 * LƯU Ý QUAN TRỌNG: DAO này gọi DBConnection.getConnection() - đây là
 * class util/DBConnection.java ĐÃ CÓ SẴN trong project (không tạo lại).
 * Nếu class đó có tên phương thức lấy Connection khác (không phải
 * getConnection()), bạn chỉ cần đổi đúng 1 chỗ: dòng gọi trong mỗi
 * phương thức bên dưới.
 *
 * Kỹ thuật chống trùng: UPDATE ... WHERE status = 'UNCONFIRMED' AND file_version = ?
 * Đây là một câu lệnh ATOMIC ở tầng CSDL: nếu 2 request đến gần như đồng thời
 * (double-click, 2 tab, mất kết nối rồi bấm lại), chỉ request đầu tiên khớp
 * điều kiện WHERE và cập nhật được (executeUpdate() trả về 1), các request
 * sau đó điều kiện không còn đúng nữa (status đã đổi) nên trả về 0 dòng ảnh
 * hưởng -> DAO trả false, tầng Controller dựa vào đó để báo "đã được xác
 * nhận trước đó" thay vì ghi thêm một bản ghi/log xác nhận mới.
 */
public class ContractDAO {

    public Contract findById(Long id) {
        String sql = "SELECT * FROM contracts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Thực tập sinh chỉ được xem hợp đồng của chính mình -> lọc kèm internProfileId. */
    public Contract findByIdForIntern(Long contractId, Long internProfileId) {
        String sql = "SELECT * FROM contracts WHERE id = ? AND intern_profile_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setLong(2, internProfileId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Contract findActiveByInternProfileId(Long internProfileId) {
        String sql = "SELECT * FROM contracts WHERE intern_profile_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, internProfileId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Đánh dấu "đã mở xem" cho đúng fileVersion hiện tại (chỉ ghi lần đầu,
     * các lần xem sau không ghi đè để giữ đúng mốc thời gian mở xem đầu tiên).
     */
    public void markViewedIfFirstTime(Long contractId, Integer fileVersion) {
        String sql = "UPDATE contracts SET viewed_at = NOW() " +
                "WHERE id = ? AND file_version = ? AND viewed_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setInt(2, fileVersion);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Xác nhận hợp đồng (nguồn: INTERN hoặc HR).
     * @return true nếu LẦN NÀY là lần ghi nhận xác nhận thành công;
     *         false nếu hợp đồng đã ở trạng thái khác UNCONFIRMED từ trước
     *         (tức đã được xác nhận / đang yêu cầu chỉnh) -> không ghi trùng.
     */
    public boolean confirm(Long contractId, Integer expectedFileVersion, Long actorUserId, String source) {
        String sql = "UPDATE contracts " +
                "SET status = 'CONFIRMED', confirmed_by_user_id = ?, confirm_source = ?, confirmed_at = NOW() " +
                "WHERE id = ? AND status = 'UNCONFIRMED' AND file_version = ? AND viewed_at IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, actorUserId);
            ps.setString(2, source);
            ps.setLong(3, contractId);
            ps.setInt(4, expectedFileVersion);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Gửi yêu cầu chỉnh, cùng cơ chế atomic như confirm() để tránh gửi trùng. */
    public boolean requestAmendment(Long contractId, Integer expectedFileVersion, String reason) {
        String sql = "UPDATE contracts " +
                "SET status = 'AMENDMENT_REQUESTED', amendment_reason = ?, amendment_requested_at = NOW() " +
                "WHERE id = ? AND status = 'UNCONFIRMED' AND file_version = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setLong(2, contractId);
            ps.setInt(3, expectedFileVersion);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** HR xóa file cũ & gắn file mới: tăng fileVersion, về UNCONFIRMED, xóa mọi dấu vết xác nhận/yêu cầu chỉnh cũ. */
    public boolean reuploadFile(Long contractId, String newPdfUrl) {
        String sql = "UPDATE contracts SET contract_pdf_url = ?, file_version = file_version + 1, " +
                "status = 'UNCONFIRMED', viewed_at = NULL, " +
                "confirmed_by_user_id = NULL, confirm_source = NULL, confirmed_at = NULL, " +
                "amendment_reason = NULL, amendment_requested_at = NULL " +
                "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPdfUrl);
            ps.setLong(2, contractId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** HR đóng yêu cầu chỉnh mà không đổi file (đã trao đổi ngoài hệ thống) -> về UNCONFIRMED. */
    public boolean closeAmendmentRequest(Long contractId) {
        String sql = "UPDATE contracts SET status = 'UNCONFIRMED', amendment_reason = NULL, " +
                "amendment_requested_at = NULL WHERE id = ? AND status = 'AMENDMENT_REQUESTED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * HR sửa ngày bắt đầu/kết thúc. Theo quy tắc: nếu hợp đồng đang
     * AMENDMENT_REQUESTED thì sửa xong chuyển về UNCONFIRMED (một trong ba
     * cách xử lý yêu cầu chỉnh); nếu đang CONFIRMED thì GIỮ NGUYÊN trạng
     * thái (chỉ sửa ngày), việc cảnh báo + ghi log cũ/mới do tầng Controller
     * đảm nhiệm trước khi gọi hàm này.
     */
    public boolean editDates(Long contractId, java.sql.Date effectiveDate, java.sql.Date expirationDate) {
        Contract current = findById(contractId);
        if (current == null) return false;

        boolean resolvingAmendment = "AMENDMENT_REQUESTED".equals(current.getStatus());
        String sql = resolvingAmendment
                ? "UPDATE contracts SET effective_date = ?, expiration_date = ?, status = 'UNCONFIRMED', " +
                  "amendment_reason = NULL, amendment_requested_at = NULL WHERE id = ?"
                : "UPDATE contracts SET effective_date = ?, expiration_date = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, effectiveDate);
            ps.setDate(2, expirationDate);
            ps.setLong(3, contractId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Contract mapRow(ResultSet rs) throws SQLException {
        Contract c = new Contract();
        c.setId(rs.getLong("id"));
        c.setContractNumber(rs.getString("contract_number"));
        c.setInternProfileId(rs.getLong("intern_profile_id"));
        c.setEffectiveDate(rs.getDate("effective_date"));
        c.setExpirationDate(rs.getDate("expiration_date"));
        c.setMonthlyAllowance(rs.getBigDecimal("monthly_allowance"));
        c.setContractPdfUrl(rs.getString("contract_pdf_url"));
        c.setFileVersion(rs.getInt("file_version"));
        c.setStatus(rs.getString("status"));
        c.setViewedAt(rs.getTimestamp("viewed_at"));
        long confirmedBy = rs.getLong("confirmed_by_user_id");
        c.setConfirmedByUserId(rs.wasNull() ? null : confirmedBy);
        c.setConfirmSource(rs.getString("confirm_source"));
        c.setConfirmedAt(rs.getTimestamp("confirmed_at"));
        c.setAmendmentReason(rs.getString("amendment_reason"));
        c.setAmendmentRequestedAt(rs.getTimestamp("amendment_requested_at"));
        return c;
    }
}
