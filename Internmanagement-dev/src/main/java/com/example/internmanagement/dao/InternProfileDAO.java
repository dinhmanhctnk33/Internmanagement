package com.example.internmanagement.dao;

import com.example.internmanagement.model.InternProfile;
import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InternProfileDAO {

    private static final String PROFILE_SELECT = """
            SELECT
                ip.id,
                ip.user_id,
                ip.intern_code,
                ip.identity_number,
                ip.date_of_birth,
                ip.gender,
                ip.address,
                ip.university_name,
                ip.major_name,
                ip.program_id,
                ip.mentor_id,
                ip.start_date,
                ip.end_date,
                ip.bank_account_no,
                ip.bank_name,
                ip.internship_status,
                ip.phone_number,
                u.full_name AS user_full_name,
                u.email AS user_email,
                mentor_user.full_name AS mentor_name
            FROM intern_profiles ip
            JOIN users u ON u.id = ip.user_id
            LEFT JOIN mentors m ON m.id = ip.mentor_id
            LEFT JOIN users mentor_user ON mentor_user.id = m.user_id
            """;

    public List<InternProfile> getAllInterns() throws SQLException {
        String sql = PROFILE_SELECT + " ORDER BY ip.id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            return mapProfiles(results);
        }
    }

    public List<InternProfile> searchInterns(String keyword) throws SQLException {
        return searchInterns(keyword, null, null);
    }

    public List<InternProfile> searchInterns(String keyword, String university, String major) throws SQLException {
        StringBuilder sql = new StringBuilder(PROFILE_SELECT).append(" WHERE 1 = 1");
        String query = "%" + (keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT)) + "%";
        List<String> parameters = new ArrayList<>();
        sql.append(" AND (LOWER(ip.intern_code) LIKE ?")
           .append(" OR LOWER(COALESCE(u.full_name, '')) LIKE ?")
           .append(" OR LOWER(COALESCE(u.email, '')) LIKE ?")
           .append(" OR LOWER(COALESCE(ip.university_name, '')) LIKE ?")
           .append(" OR LOWER(COALESCE(ip.major_name, '')) LIKE ?")
           .append(" OR LOWER(COALESCE(ip.phone_number, '')) LIKE ?")
           .append(" OR LOWER(ip.internship_status) LIKE ?)");
        for (int i = 0; i < 7; i++) {
            parameters.add(query);
        }
        if (university != null && !university.isBlank()) {
            sql.append(" AND LOWER(COALESCE(ip.university_name, '')) = LOWER(?)");
            parameters.add(university.trim());
        }
        if (major != null && !major.isBlank()) {
            sql.append(" AND LOWER(COALESCE(ip.major_name, '')) = LOWER(?)");
            parameters.add(major.trim());
        }
        sql.append(" ORDER BY ip.id DESC");
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setString(i + 1, parameters.get(i));
            }
            try (ResultSet results = statement.executeQuery()) {
                return mapProfiles(results);
            }
        }
    }

    public List<String> findDistinctUniversities() throws SQLException {
        return findDistinctProfileValues("university_name");
    }

    public List<String> findDistinctMajors() throws SQLException {
        return findDistinctProfileValues("major_name");
    }

    private List<String> findDistinctProfileValues(String columnName) throws SQLException {
        String sql = "SELECT DISTINCT TRIM(" + columnName + ") AS value "
                + "FROM intern_profiles WHERE " + columnName + " IS NOT NULL "
                + "AND TRIM(" + columnName + ") <> '' ORDER BY value";
        List<String> values = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                values.add(results.getString("value"));
            }
        }
        return values;
    }

    public InternProfile getInternById(int id) throws SQLException {
        String sql = PROFILE_SELECT + " WHERE ip.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? mapProfile(results) : null;
            }
        }
    }

    public InternProfile findByCode(String internCode) throws SQLException {
        String sql = PROFILE_SELECT + " WHERE ip.intern_code = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, internCode);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? mapProfile(results) : null;
            }
        }
    }

    public InternProfile findByUserId(int userId) throws SQLException {
        String sql = PROFILE_SELECT + " WHERE ip.user_id = ? LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? mapProfile(results) : null;
            }
        }
    }

    public Integer findActiveInternUserIdByEmail(String email) throws SQLException {
        String sql = """
                SELECT u.id
                FROM users u
                JOIN user_roles ur ON ur.user_id = u.id
                JOIN roles r ON r.id = ur.role_id
                WHERE LOWER(u.email) = LOWER(?)
                  AND u.status = 'ACTIVE'
                  AND r.role_code = 'INTERN'
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? results.getInt("id") : null;
            }
        }
    }

    /**
     * Returns active Intern accounts that may be linked to a profile.  The currently
     * linked account is included when editing so its name can still be displayed.
     */
    public List<User> findSelectableInternUsers(int includedUserId) throws SQLException {
        String sql = """
                SELECT DISTINCT u.id, u.full_name, u.email
                FROM users u
                JOIN user_roles ur ON ur.user_id = u.id
                JOIN roles r ON r.id = ur.role_id
                LEFT JOIN intern_profiles ip ON ip.user_id = u.id
                WHERE u.status = 'ACTIVE'
                  AND r.role_code = 'INTERN'
                  AND (ip.id IS NULL OR u.id = ?)
                ORDER BY u.full_name, u.email
                """;
        List<User> users = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, includedUserId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    User user = new User();
                    user.setId(results.getInt("id"));
                    user.setFullName(results.getString("full_name"));
                    user.setEmail(results.getString("email"));
                    users.add(user);
                }
            }
        }
        return users;
    }

    public Integer findMentorIdByName(String fullName) throws SQLException {
        String sql = """
                SELECT m.id
                FROM mentors m
                JOIN users u ON u.id = m.user_id
                WHERE LOWER(u.full_name) = LOWER(?)
                  AND u.status = 'ACTIVE'
                  AND m.status = 'ACTIVE'
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName.trim());
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                int mentorId = results.getInt("id");
                if (results.next()) {
                    return null;
                }
                return mentorId;
            }
        }
    }

    public void insert(InternProfile profile) throws SQLException {
        requirePersistableProfile(profile);
        String sql = """
                INSERT INTO intern_profiles (
                    user_id, intern_code, identity_number, date_of_birth, gender, address,
                    university_name, major_name, program_id, mentor_id, start_date, end_date,
                    bank_account_no, bank_name, internship_status, phone_number
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            setProfileParameters(statement, profile);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    profile.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Không nhận được ID hồ sơ thực tập sinh mới.");
                }
            }
        }
    }

    public void update(InternProfile profile) throws SQLException {
        requirePersistableProfile(profile);
        if (profile.getId() <= 0) {
            throw new IllegalArgumentException("ID hồ sơ không hợp lệ để cập nhật.");
        }

        String sql = """
                UPDATE intern_profiles SET
                    user_id = ?, intern_code = ?, identity_number = ?, date_of_birth = ?,
                    gender = ?, address = ?, university_name = ?, major_name = ?,
                    program_id = ?, mentor_id = ?, start_date = ?, end_date = ?,
                    bank_account_no = ?, bank_name = ?, internship_status = ?, phone_number = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setProfileParameters(statement, profile);
            statement.setInt(17, profile.getId());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Không tìm thấy hồ sơ thực tập sinh cần cập nhật.");
            }
        }
    }

    private static void requirePersistableProfile(InternProfile profile) {
        if (profile.getUserId() <= 0) {
            throw new IllegalArgumentException("Hồ sơ phải được liên kết với một tài khoản thực tập sinh.");
        }
        if (profile.getInternCode() == null || profile.getInternCode().isBlank()) {
            throw new IllegalArgumentException("Mã thực tập sinh không được để trống.");
        }
        if (profile.getInternshipStatus() == null || profile.getInternshipStatus().isBlank()) {
            profile.setInternshipStatus("ACTIVE");
        }
        if (!List.of("ACTIVE", "COMPLETED", "PAUSED", "DROPPED")
                .contains(profile.getInternshipStatus().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Trạng thái thực tập không hợp lệ.");
        }
        profile.setInternshipStatus(profile.getInternshipStatus().toUpperCase(Locale.ROOT));
    }

    private static void setProfileParameters(PreparedStatement statement, InternProfile profile)
            throws SQLException {
        statement.setInt(1, profile.getUserId());
        statement.setString(2, profile.getInternCode());
        statement.setString(3, profile.getIdentityNumber());
        setDate(statement, 4, profile.getDateOfBirth());
        statement.setString(5, profile.getGender());
        statement.setString(6, profile.getAddress());
        statement.setString(7, profile.getUniversityName());
        statement.setString(8, profile.getMajorName());
        setNullableId(statement, 9, profile.getProgramId());
        setNullableId(statement, 10, profile.getMentorId());
        setDate(statement, 11, profile.getStartDate());
        setDate(statement, 12, profile.getEndDate());
        statement.setString(13, profile.getBankAccountNo());
        statement.setString(14, profile.getBankName());
        statement.setString(15, profile.getInternshipStatus());
        statement.setString(16, profile.getPhoneNumber());
    }

    private static void setDate(PreparedStatement statement, int index, java.time.LocalDate value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, java.sql.Date.valueOf(value));
        }
    }

    private static void setNullableId(PreparedStatement statement, int index, int value)
            throws SQLException {
        if (value <= 0) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setInt(index, value);
        }
    }

    private static List<InternProfile> mapProfiles(ResultSet results) throws SQLException {
        List<InternProfile> profiles = new ArrayList<>();
        while (results.next()) {
            profiles.add(mapProfile(results));
        }
        return profiles;
    }

    private static InternProfile mapProfile(ResultSet results) throws SQLException {
        InternProfile profile = new InternProfile();
        profile.setId(results.getInt("id"));
        profile.setUserId(results.getInt("user_id"));
        profile.setInternCode(results.getString("intern_code"));
        profile.setIdentityNumber(results.getString("identity_number"));

        java.sql.Date dateOfBirth = results.getDate("date_of_birth");
        if (dateOfBirth != null) {
            profile.setDateOfBirth(dateOfBirth.toLocalDate());
        }

        profile.setGender(results.getString("gender"));
        profile.setAddress(results.getString("address"));
        profile.setUniversityName(results.getString("university_name"));
        profile.setMajorName(results.getString("major_name"));
        profile.setProgramId(results.getInt("program_id"));
        profile.setMentorId(results.getInt("mentor_id"));

        java.sql.Date startDate = results.getDate("start_date");
        if (startDate != null) {
            profile.setStartDate(startDate.toLocalDate());
        }

        java.sql.Date endDate = results.getDate("end_date");
        if (endDate != null) {
            profile.setEndDate(endDate.toLocalDate());
        }

        profile.setBankAccountNo(results.getString("bank_account_no"));
        profile.setBankName(results.getString("bank_name"));
        profile.setInternshipStatus(results.getString("internship_status"));
        profile.setPhoneNumber(results.getString("phone_number"));
        profile.setFullName(results.getString("user_full_name"));
        profile.setEmail(results.getString("user_email"));
        profile.setMentorName(results.getString("mentor_name"));
        return profile;
    }
}
