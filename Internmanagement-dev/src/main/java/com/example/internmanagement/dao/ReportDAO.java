package com.example.internmanagement.dao;

import com.example.internmanagement.model.ReportPayload;
import com.example.internmanagement.model.ReportSnapshot;
import com.example.internmanagement.util.DBConnection;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Tổng hợp dữ liệu báo cáo cuối kỳ, tạo xem trước và lưu bản chụp bất biến.
 * Quy ước: đánh giá "Đã chốt" = intern_evaluations.hr_approval_status IN ('APPROVED','PUBLISHED').
 */
public class ReportDAO {
    public static final String NO_SCHOOL = "Chưa có thông tin trường";
    public static final int MIN_GROUP = 3; // nhóm dưới 3 người thì ẩn thống kê
    private static final String NO_DEPT = "Chưa có phòng ban";
    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Dữ liệu tạm của một thực tập sinh trong lúc tổng hợp. */
    private static class InternData {
        long id;
        long programId;
        String fullName, school, programName, deptName, comments;
        LocalDate start, end;
        boolean evaluated;
        Double tech, attitude, punctuality, finalScore;
        int attended;
        int weekly;
        Integer expected;
        Double rate;
    }

    // ------------------------------------------------------------------
    // Danh sách chương trình để chọn trên màn hình tạo báo cáo
    // ------------------------------------------------------------------
    public List<Map<String, Object>> programs() throws SQLException {
        String sql = "SELECT p.id, p.program_code, p.program_name, d.dept_name "
                + "FROM internship_programs p LEFT JOIN departments d ON d.id = p.department_id "
                + "ORDER BY p.program_name";
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getLong("id"));
                row.put("programCode", rs.getString("program_code"));
                row.put("programName", rs.getString("program_name"));
                row.put("deptName", rs.getString("dept_name"));
                result.add(row);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // XEM TRƯỚC (chưa lưu gì vào database)
    // ------------------------------------------------------------------
    public Map<String, Object> preview(String reportType, List<Long> programIds) throws SQLException {
        checkType(reportType);
        try (Connection c = DBConnection.getConnection()) {
            Map<Long, String> names = programNames(c, programIds);
            if (names.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất một chương trình.");
            List<InternData> interns = loadInterns(c, new ArrayList<>(names.keySet()));

            int finalized = 0;
            List<String> notFinalized = new ArrayList<>();
            for (InternData i : interns) {
                if (i.evaluated) finalized++;
                else notFinalized.add(i.fullName + " (" + i.programName + ")");
            }

            List<String> emptyPrograms = new ArrayList<>();
            for (Map.Entry<Long, String> e : names.entrySet()) {
                long total = interns.stream().filter(i -> i.programId == e.getKey()).count();
                long fin = interns.stream().filter(i -> i.programId == e.getKey() && i.evaluated).count();
                if (total == 0) emptyPrograms.add(e.getValue() + " (chưa có thực tập sinh)");
                else if (fin == 0) emptyPrograms.add(e.getValue() + " (chưa ai có đánh giá đã chốt)");
            }

            List<Map<String, Object>> schools = new ArrayList<>();
            if (ReportSnapshot.TYPE_SCHOOL.equals(reportType)) {
                Map<String, Integer> directory = directoryCounts(c);
                for (Map.Entry<String, List<InternData>> e : groupBySchool(interns).entrySet()) {
                    boolean noSchool = NO_SCHOOL.equals(e.getKey());
                    int emailCount = noSchool ? 0 : directory.getOrDefault(normalizeSchool(e.getKey()), 0);
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("schoolName", e.getKey());
                    row.put("internCount", e.getValue().size());
                    row.put("noSchoolInfo", noSchool);
                    row.put("emailCount", emailCount);
                    row.put("inDirectory", emailCount > 0);
                    schools.add(row);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("reportType", reportType);
            result.put("programIds", new ArrayList<>(names.keySet()));
            result.put("programNames", String.join(", ", names.values()));
            result.put("internCount", interns.size());
            result.put("finalizedCount", finalized);
            result.put("notFinalized", notFinalized);
            result.put("emptyPrograms", emptyPrograms);
            result.put("schools", schools);
            return result;
        }
    }

    // ------------------------------------------------------------------
    // LƯU BẢN CHỤP (transaction: lỗi giữa chừng thì không để lại bản dở dang)
    // ------------------------------------------------------------------
    public long saveSnapshot(String reportType, List<Long> programIds, long userId,
                             String userName, String ip) throws SQLException {
        checkType(reportType);
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                Map<Long, String> names = programNames(c, programIds);
                if (names.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất một chương trình.");
                List<InternData> interns = loadInterns(c, new ArrayList<>(names.keySet()));

                LocalDateTime now = LocalDateTime.now(); // giờ máy chủ
                ReportPayload payload = new ReportPayload();
                payload.reportType = reportType;
                payload.programNames = String.join(", ", names.values());
                payload.createdAt = now.format(DATETIME);
                payload.createdByName = userName;
                if (ReportSnapshot.TYPE_LEADER.equals(reportType)) payload.leader = buildLeader(interns, names);
                else payload.schools = buildSchools(interns);

                String ids = names.keySet().stream().sorted().map(String::valueOf)
                        .collect(Collectors.joining(","));
                long snapshotId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO report_snapshots(report_type, program_ids, program_names, payload_json, created_by, created_at) "
                                + "VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, reportType);
                    ps.setString(2, ids);
                    ps.setString(3, payload.programNames);
                    ps.setString(4, GSON.toJson(payload));
                    ps.setLong(5, userId);
                    ps.setTimestamp(6, Timestamp.valueOf(now));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không lấy được mã bản chụp.");
                        snapshotId = keys.getLong(1);
                    }
                }
                Map<String, Object> audit = new LinkedHashMap<>();
                audit.put("reportType", reportType);
                audit.put("programIds", ids);
                audit(c, userId, "CREATE_REPORT_SNAPSHOT", "report_snapshots", snapshotId, GSON.toJson(audit), ip);
                c.commit();
                return snapshotId;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------
    // Lịch sử / đọc bản chụp
    // ------------------------------------------------------------------
    public List<ReportSnapshot> listSnapshots() throws SQLException {
        String sql = "SELECT s.id, s.report_type, s.program_ids, s.program_names, s.created_by, u.full_name, s.created_at, "
                + "EXISTS(SELECT 1 FROM report_snapshots n WHERE n.report_type = s.report_type "
                + "AND n.program_ids = s.program_ids AND n.id > s.id) AS newer "
                + "FROM report_snapshots s LEFT JOIN users u ON u.id = s.created_by "
                + "ORDER BY s.id DESC LIMIT 200";
        List<ReportSnapshot> result = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ReportSnapshot s = new ReportSnapshot();
                s.setId(rs.getLong("id"));
                s.setReportType(rs.getString("report_type"));
                s.setProgramIds(rs.getString("program_ids"));
                s.setProgramNames(rs.getString("program_names"));
                long by = rs.getLong("created_by");
                s.setCreatedBy(rs.wasNull() ? null : by);
                s.setCreatedByName(rs.getString("full_name"));
                s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().format(DATETIME));
                s.setNewerExists(rs.getBoolean("newer"));
                result.add(s);
            }
        }
        return result;
    }

    /** Trả về null nếu không tồn tại. Có kèm payloadJson. */
    public ReportSnapshot findSnapshot(long id) throws SQLException {
        String sql = "SELECT s.id, s.report_type, s.program_ids, s.program_names, s.payload_json, s.created_by, "
                + "u.full_name, s.created_at FROM report_snapshots s "
                + "LEFT JOIN users u ON u.id = s.created_by WHERE s.id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ReportSnapshot s = new ReportSnapshot();
                s.setId(rs.getLong("id"));
                s.setReportType(rs.getString("report_type"));
                s.setProgramIds(rs.getString("program_ids"));
                s.setProgramNames(rs.getString("program_names"));
                s.setPayloadJson(rs.getString("payload_json"));
                long by = rs.getLong("created_by");
                s.setCreatedBy(rs.wasNull() ? null : by);
                s.setCreatedByName(rs.getString("full_name"));
                s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().format(DATETIME));
                return s;
            }
        }
    }

    public static ReportPayload readPayload(ReportSnapshot snapshot) {
        return GSON.fromJson(snapshot.getPayloadJson(), ReportPayload.class);
    }

    /** Ghi nhật ký thao tác (dùng chung cho các DAO/servlet khác của chức năng này). */
    public static void audit(Connection c, long userId, String action, String table,
                             Long recordId, String json, String ip) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO audit_logs(user_id, action_code, table_name, record_id, new_values_json, ip_address) "
                        + "VALUES(?,?,?,?,?,?)")) {
            ps.setLong(1, userId);
            ps.setString(2, action);
            ps.setString(3, table);
            if (recordId == null) ps.setNull(4, java.sql.Types.BIGINT);
            else ps.setLong(4, recordId);
            ps.setString(5, json);
            ps.setString(6, ip);
            ps.executeUpdate();
        }
    }

    /** Chuẩn hóa tên trường để so khớp với danh bạ (bỏ khoảng trắng thừa, không phân biệt hoa thường). */
    public static String normalizeSchool(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    // ==================================================================
    // XÂY DỰNG NỘI DUNG BÁO CÁO
    // ==================================================================

    /** Báo cáo cho lãnh đạo: chỉ thống kê, không có tên/điểm/nhận xét cá nhân. */
    private ReportPayload.Leader buildLeader(List<InternData> interns, Map<Long, String> names) {
        List<ReportPayload.ProgramStat> programs = new ArrayList<>();
        for (Map.Entry<Long, String> entry : names.entrySet()) {
            List<InternData> in = new ArrayList<>();
            for (InternData i : interns) if (i.programId == entry.getKey()) in.add(i);
            List<InternData> fin = in.stream().filter(i -> i.evaluated).toList();

            // Dưới 3 người đã chốt: ẩn điểm + phân bố. (Nếu = 0 người thì giao diện ghi "chưa có dữ liệu".)
            boolean hidden = fin.size() < MIN_GROUP;
            Double avgTech = null, avgAtt = null, avgFinal = null;
            int[] distribution = null;
            if (!hidden) {
                avgTech = avg(fin.stream().map(i -> i.tech).toList());
                avgAtt = avg(fin.stream().map(i -> i.attitude).toList());
                avgFinal = avg(fin.stream().map(i -> i.finalScore).toList());
                distribution = new int[4];
                for (InternData i : fin) {
                    if (i.finalScore == null) continue;
                    double s = i.finalScore;
                    int bucket = s < 2.0 ? 0 : s < 3.0 ? 1 : s < 4.0 ? 2 : 3;
                    distribution[bucket]++;
                }
            }
            Double avgAttendance = in.size() < MIN_GROUP ? null
                    : avg(in.stream().map(i -> i.rate).toList());
            int weekly = in.stream().mapToInt(i -> i.weekly).sum();

            // Thống kê theo phòng ban trong chương trình
            Map<String, List<InternData>> byDept = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (InternData i : in) {
                String dept = i.deptName == null || i.deptName.isBlank() ? NO_DEPT : i.deptName;
                byDept.computeIfAbsent(dept, k -> new ArrayList<>()).add(i);
            }
            List<ReportPayload.DepartmentStat> depts = new ArrayList<>();
            for (Map.Entry<String, List<InternData>> d : byDept.entrySet()) {
                List<InternData> dfin = d.getValue().stream().filter(i -> i.evaluated).toList();
                boolean dHidden = dfin.size() < MIN_GROUP;
                depts.add(new ReportPayload.DepartmentStat(
                        d.getKey(), d.getValue().size(), dfin.size(), dHidden,
                        dHidden ? null : avg(dfin.stream().map(i -> i.tech).toList()),
                        dHidden ? null : avg(dfin.stream().map(i -> i.attitude).toList()),
                        dHidden ? null : avg(dfin.stream().map(i -> i.finalScore).toList())));
            }

            programs.add(new ReportPayload.ProgramStat(
                    entry.getValue(), in.size(), fin.size(), in.size() - fin.size(), hidden,
                    avgTech, avgAtt, avgFinal, distribution, avgAttendance,
                    weekly, null, depts));
        }
        return new ReportPayload.Leader(programs);
    }

    /** Báo cáo cho trường: mỗi trường một mục, chỉ chứa thực tập sinh của trường đó. */
    private List<ReportPayload.School> buildSchools(List<InternData> interns) {
        List<ReportPayload.School> schools = new ArrayList<>();
        for (Map.Entry<String, List<InternData>> e : groupBySchool(interns).entrySet()) {
            List<ReportPayload.InternRow> rows = new ArrayList<>();
            for (InternData i : e.getValue()) {
                rows.add(new ReportPayload.InternRow(
                        i.fullName, i.programName, i.deptName,
                        i.start == null ? null : i.start.format(DATE),
                        i.end == null ? null : i.end.format(DATE),
                        i.evaluated,
                        i.evaluated ? i.tech : null,
                        i.evaluated ? i.attitude : null,
                        i.evaluated ? i.punctuality : null,
                        i.evaluated ? i.finalScore : null,
                        null, // không kết luận đạt/không đạt (ngoài phạm vi story)
                        i.evaluated ? i.comments : null,
                        i.attended, i.expected));
            }
            schools.add(new ReportPayload.School(e.getKey(), NO_SCHOOL.equals(e.getKey()), rows));
        }
        return schools;
    }

    /** Gom theo tên trường. Viết khác nhau = trường khác nhau (để HR thấy và sửa). Nhóm "chưa có trường" xếp cuối. */
    private Map<String, List<InternData>> groupBySchool(List<InternData> interns) {
        Map<String, List<InternData>> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (InternData i : interns) {
            String key = i.school == null || i.school.isBlank() ? NO_SCHOOL : i.school.trim();
            sorted.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }
        Map<String, List<InternData>> result = new LinkedHashMap<>();
        List<InternData> noSchool = null;
        for (Map.Entry<String, List<InternData>> e : sorted.entrySet()) {
            if (NO_SCHOOL.equalsIgnoreCase(e.getKey())) noSchool = e.getValue();
            else result.put(e.getKey(), e.getValue());
        }
        if (noSchool != null) result.put(NO_SCHOOL, noSchool);
        return result;
    }

    // ==================================================================
    // TRUY VẤN DỮ LIỆU NGUỒN
    // ==================================================================

    private Map<Long, String> programNames(Connection c, List<Long> ids) throws SQLException {
        Map<Long, String> result = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) return result;
        String sql = "SELECT id, program_name FROM internship_programs WHERE id IN (" + marks(ids.size())
                + ") ORDER BY program_name";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bindIds(ps, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.put(rs.getLong(1), rs.getString(2));
            }
        }
        return result;
    }

    /** Thực tập sinh (ACTIVE hoặc COMPLETED) của các chương trình + đánh giá đã chốt mới nhất + chuyên cần + báo cáo tuần. */
    private List<InternData> loadInterns(Connection c, List<Long> programIds) throws SQLException {
        Map<Long, InternData> map = new LinkedHashMap<>();
        String sql = "SELECT ip.id, u.full_name, ip.university_name, ip.start_date, ip.end_date, "
                + "p.id AS program_id, p.program_name, d.dept_name, ie.id AS eval_id, "
                + "ie.technical_score, ie.attitude_score, ie.punctuality_score, ie.final_score, ie.overall_comments "
                + "FROM intern_profiles ip JOIN users u ON u.id = ip.user_id "
                + "JOIN internship_programs p ON p.id = ip.program_id "
                + "LEFT JOIN departments d ON d.id = p.department_id "
                + "LEFT JOIN intern_evaluations ie ON ie.id = (SELECT MAX(x.id) FROM intern_evaluations x "
                + "WHERE x.intern_profile_id = ip.id AND x.hr_approval_status IN ('APPROVED','PUBLISHED')) "
                + "WHERE ip.program_id IN (" + marks(programIds.size()) + ") "
                + "AND ip.internship_status IN ('ACTIVE','COMPLETED') "
                + "ORDER BY p.program_name, u.full_name";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bindIds(ps, programIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InternData i = new InternData();
                    i.id = rs.getLong("id");
                    i.fullName = rs.getString("full_name");
                    i.school = rs.getString("university_name");
                    i.start = rs.getObject("start_date", LocalDate.class);
                    i.end = rs.getObject("end_date", LocalDate.class);
                    i.programId = rs.getLong("program_id");
                    i.programName = rs.getString("program_name");
                    i.deptName = rs.getString("dept_name");
                    rs.getLong("eval_id");
                    i.evaluated = !rs.wasNull();
                    i.tech = dbl(rs, "technical_score");
                    i.attitude = dbl(rs, "attitude_score");
                    i.punctuality = dbl(rs, "punctuality_score");
                    i.finalScore = dbl(rs, "final_score");
                    i.comments = rs.getString("overall_comments");
                    map.put(i.id, i);
                }
            }
        }
        if (map.isEmpty()) return new ArrayList<>();

        // Số ngày đi làm (có check-in) trong thời gian thực tập của từng người
        String attSql = "SELECT ar.intern_profile_id, COUNT(*) FROM attendance_records ar "
                + "JOIN intern_profiles ip ON ip.id = ar.intern_profile_id "
                + "WHERE ip.program_id IN (" + marks(programIds.size()) + ") AND ar.check_in_time IS NOT NULL "
                + "AND (ip.start_date IS NULL OR ar.work_date >= ip.start_date) "
                + "AND (ip.end_date IS NULL OR ar.work_date <= ip.end_date) GROUP BY ar.intern_profile_id";
        try (PreparedStatement ps = c.prepareStatement(attSql)) {
            bindIds(ps, programIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InternData i = map.get(rs.getLong(1));
                    if (i != null) i.attended = rs.getInt(2);
                }
            }
        }

        // Số báo cáo tuần đã nộp
        String weeklySql = "SELECT wr.intern_profile_id, COUNT(*) FROM weekly_reports wr "
                + "JOIN intern_profiles ip ON ip.id = wr.intern_profile_id "
                + "WHERE ip.program_id IN (" + marks(programIds.size()) + ") GROUP BY wr.intern_profile_id";
        try (PreparedStatement ps = c.prepareStatement(weeklySql)) {
            bindIds(ps, programIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InternData i = map.get(rs.getLong(1));
                    if (i != null) i.weekly = rs.getInt(2);
                }
            }
        }

        // Ngày nghỉ có phép (đã duyệt) để trừ khỏi số ngày kỳ vọng
        Map<Long, List<LocalDate[]>> leaves = new LinkedHashMap<>();
        String leaveSql = "SELECT lr.intern_profile_id, lr.start_date, lr.end_date FROM leave_requests lr "
                + "JOIN intern_profiles ip ON ip.id = lr.intern_profile_id "
                + "WHERE ip.program_id IN (" + marks(programIds.size()) + ") AND lr.approval_status = 'APPROVED'";
        try (PreparedStatement ps = c.prepareStatement(leaveSql)) {
            bindIds(ps, programIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    leaves.computeIfAbsent(rs.getLong(1), k -> new ArrayList<>()).add(new LocalDate[]{
                            rs.getObject(2, LocalDate.class), rs.getObject(3, LocalDate.class)});
                }
            }
        }

        // Số ngày kỳ vọng = ngày làm việc (T2-T6) từ ngày bắt đầu đến min(ngày kết thúc, hôm nay) trừ nghỉ có phép
        LocalDate today = LocalDate.now();
        for (InternData i : map.values()) {
            if (i.start == null) continue; // thiếu ngày bắt đầu: "chưa có dữ liệu"
            LocalDate endEff = i.end == null || i.end.isAfter(today) ? today : i.end;
            int leaveDays = 0;
            for (LocalDate[] l : leaves.getOrDefault(i.id, List.of())) {
                if (l[0] == null || l[1] == null) continue;
                LocalDate from = l[0].isAfter(i.start) ? l[0] : i.start;
                LocalDate to = l[1].isBefore(endEff) ? l[1] : endEff;
                leaveDays += weekdays(from, to);
            }
            i.expected = Math.max(0, weekdays(i.start, endEff) - leaveDays);
            if (i.expected > 0) i.rate = round(Math.min(100.0, i.attended * 100.0 / i.expected), 1);
        }
        return new ArrayList<>(map.values());
    }

    /** Số email trong danh bạ theo tên trường (đã chuẩn hóa). */
    private Map<String, Integer> directoryCounts(Connection c) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT organization_name, COUNT(*) FROM report_recipients "
                + "WHERE recipient_type = 'SCHOOL' GROUP BY organization_name";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.merge(normalizeSchool(rs.getString(1)), rs.getInt(2), Integer::sum);
        }
        return result;
    }

    // ==================================================================
    // HÀM PHỤ
    // ==================================================================
    private static void checkType(String type) {
        if (!ReportSnapshot.TYPE_LEADER.equals(type) && !ReportSnapshot.TYPE_SCHOOL.equals(type))
            throw new IllegalArgumentException("Loại báo cáo không hợp lệ.");
    }

    private static String marks(int n) {
        return String.join(",", Collections.nCopies(n, "?"));
    }

    private static void bindIds(PreparedStatement ps, List<Long> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) ps.setLong(i + 1, ids.get(i));
    }

    private static Double dbl(ResultSet rs, String column) throws SQLException {
        BigDecimal v = rs.getBigDecimal(column);
        return v == null ? null : v.doubleValue();
    }

    private static Double avg(List<Double> values) {
        OptionalDouble o = values.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).average();
        return o.isPresent() ? round(o.getAsDouble(), 2) : null;
    }

    private static double round(double value, int digits) {
        double f = Math.pow(10, digits);
        return Math.round(value * f) / f;
    }

    private static int weekdays(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) return 0;
        int n = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            DayOfWeek w = d.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) n++;
        }
        return n;
    }
}