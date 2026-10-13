package com.example.internmanagement.dao;

import com.example.internmanagement.model.RecipientContact;
import com.example.internmanagement.util.DBConnection;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Danh bạ người nhận báo cáo: thêm, sửa, xóa, tra cứu. */
public class RecipientDAO {
    private static final Gson GSON = new Gson();
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MYSQL_DUPLICATE = 1062;

    private static final String SELECT = "SELECT id, recipient_type, organization_name, contact_name, email, created_at "
            + "FROM report_recipients ";

    /** Danh sách có lọc: type = "" (tất cả) / SCHOOL / LEADER; keyword tìm theo đơn vị, tên liên hệ, email. */
    public List<RecipientContact> list(String type, String keyword) throws SQLException {
        String t = type == null ? "" : type.trim();
        String k = keyword == null ? "" : keyword.trim();
        String sql = SELECT + "WHERE (? = '' OR recipient_type = ?) "
                + "AND (? = '' OR organization_name LIKE CONCAT('%',?,'%') OR contact_name LIKE CONCAT('%',?,'%') "
                + "OR email LIKE CONCAT('%',?,'%')) ORDER BY recipient_type, organization_name, contact_name";
        List<RecipientContact> result = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t);
            ps.setString(2, t);
            for (int i = 3; i <= 6; i++) ps.setString(i, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public RecipientContact find(long id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Các tên trường đang có trong hồ sơ thực tập sinh (để HR chọn khi thêm người nhận). */
    public List<String> schoolNames() throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            return schoolNames(c);
        }
    }

    /** Email của đúng một trường (so khớp tên đã chuẩn hóa). */
    public List<RecipientContact> forSchool(String schoolName) throws SQLException {
        String wanted = ReportDAO.normalizeSchool(schoolName);
        List<RecipientContact> result = new ArrayList<>();
        for (RecipientContact r : list(RecipientContact.TYPE_SCHOOL, "")) {
            if (ReportDAO.normalizeSchool(r.getOrganizationName()).equals(wanted)) result.add(r);
        }
        return result;
    }

    public List<RecipientContact> leaders() throws SQLException {
        return list(RecipientContact.TYPE_LEADER, "");
    }

    /** Chỉ trả về những người nhận có thật trong danh bạ (dùng để chặn gửi tới địa chỉ ngoài danh bạ). */
    public List<RecipientContact> findByIds(List<Long> ids) throws SQLException {
        List<RecipientContact> result = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return result;
        String sql = SELECT + "WHERE id IN (" + String.join(",", Collections.nCopies(ids.size(), "?")) + ")";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) ps.setLong(i + 1, ids.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public long create(String type, String organization, String contactName, String email,
                       long userId, String ip) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            String[] v = validate(c, type, organization, contactName, email);
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO report_recipients(recipient_type, organization_name, contact_name, email, created_by) "
                            + "VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < 4; i++) ps.setString(i + 1, v[i]);
                ps.setLong(5, userId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    long id = keys.getLong(1);
                    ReportDAO.audit(c, userId, "CREATE_REPORT_RECIPIENT", "report_recipients", id, json(v), ip);
                    return id;
                }
            } catch (SQLException e) {
                throw translate(e);
            }
        }
    }

    public void update(long id, String type, String organization, String contactName, String email,
                       long userId, String ip) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            String[] v = validate(c, type, organization, contactName, email);
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE report_recipients SET recipient_type=?, organization_name=?, contact_name=?, email=? WHERE id=?")) {
                for (int i = 0; i < 4; i++) ps.setString(i + 1, v[i]);
                ps.setLong(5, id);
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Không tìm thấy người nhận.");
                ReportDAO.audit(c, userId, "UPDATE_REPORT_RECIPIENT", "report_recipients", id, json(v), ip);
            } catch (SQLException e) {
                throw translate(e);
            }
        }
    }

    /** Xóa không ảnh hưởng lịch sử gửi (lịch sử lưu sẵn địa chỉ email đã gửi). */
    public void delete(long id, long userId, String ip) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            RecipientContact old = find(id);
            if (old == null) throw new IllegalArgumentException("Không tìm thấy người nhận.");
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM report_recipients WHERE id = ?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
            ReportDAO.audit(c, userId, "DELETE_REPORT_RECIPIENT", "report_recipients", id,
                    json(new String[]{old.getRecipientType(), old.getOrganizationName(),
                            old.getContactName(), old.getEmail()}), ip);
        }
    }

    // ------------------------------------------------------------------
    /** Kiểm tra dữ liệu, trả về {type, organization, contactName, email} đã làm sạch. */
    private String[] validate(Connection c, String type, String organization, String contactName,
                              String email) throws SQLException {
        String t = clean(type);
        if (!RecipientContact.TYPE_SCHOOL.equals(t) && !RecipientContact.TYPE_LEADER.equals(t))
            throw new IllegalArgumentException("Loại người nhận không hợp lệ.");

        String org;
        if (RecipientContact.TYPE_LEADER.equals(t)) {
            org = RecipientContact.LEADER_ORG;
        } else {
            String wanted = ReportDAO.normalizeSchool(organization);
            if (wanted.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn trường.");
            org = null;
            for (String name : schoolNames(c)) { // dùng đúng tên trường trong hồ sơ
                if (ReportDAO.normalizeSchool(name).equals(wanted)) { org = name; break; }
            }
            if (org == null) throw new IllegalArgumentException("Trường phải được chọn từ danh sách trường trong hồ sơ thực tập sinh.");
        }

        String name = clean(contactName);
        if (name.isEmpty()) throw new IllegalArgumentException("Họ tên liên hệ là bắt buộc.");
        if (name.length() > 100) throw new IllegalArgumentException("Họ tên liên hệ không vượt quá 100 ký tự.");

        String mail = clean(email);
        if (mail.isEmpty()) throw new IllegalArgumentException("Email là bắt buộc.");
        if (mail.length() > 150 || !EMAIL.matcher(mail).matches())
            throw new IllegalArgumentException("Email không đúng định dạng.");
        return new String[]{t, org, name, mail};
    }

    private List<String> schoolNames(Connection c) throws SQLException {
        List<String> result = new ArrayList<>();
        String sql = "SELECT DISTINCT TRIM(university_name) FROM intern_profiles "
                + "WHERE university_name IS NOT NULL AND TRIM(university_name) <> '' ORDER BY 1";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(rs.getString(1));
        }
        return result;
    }

    private SQLException translate(SQLException e) {
        if (e.getErrorCode() == MYSQL_DUPLICATE)
            return new SQLException(new IllegalArgumentException("Email này đã có trong danh bạ của đơn vị."));
        return e;
    }

    private RecipientContact map(ResultSet rs) throws SQLException {
        return new RecipientContact(rs.getLong("id"), rs.getString("recipient_type"),
                rs.getString("organization_name"), rs.getString("contact_name"), rs.getString("email"),
                rs.getTimestamp("created_at").toLocalDateTime().format(DATETIME));
    }

    private String json(String[] v) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("type", v[0]);
        m.put("organization", v[1]);
        m.put("contactName", v[2]);
        m.put("email", v[3]);
        return GSON.toJson(m);
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}