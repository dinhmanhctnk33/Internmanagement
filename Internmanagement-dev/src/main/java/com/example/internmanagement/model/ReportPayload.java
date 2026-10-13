package com.example.internmanagement.model;

import java.util.List;

/**
 * Nội dung một bản chụp báo cáo. Lưu dưới dạng JSON trong report_snapshots.payload_json.
 * Các trường điểm kiểu Double có thể null = "chưa có dữ liệu" (không hiển thị 0).
 * Báo cáo LEADER chỉ dùng field "leader"; báo cáo SCHOOL chỉ dùng field "schools".
 */
public class ReportPayload {
    public String reportType;       // LEADER hoặc SCHOOL
    public String programNames;
    public String createdAt;
    public String createdByName;
    public Leader leader;           // chỉ có khi reportType = LEADER
    public List<School> schools;    // chỉ có khi reportType = SCHOOL

    /** Báo cáo cho lãnh đạo: chỉ có thống kê, KHÔNG có tên/điểm/nhận xét cá nhân. */
    public record Leader(List<ProgramStat> programs) {}

    public record ProgramStat(
            String programName,
            int internCount,
            int finalizedCount,          // số thực tập sinh có đánh giá "Đã chốt"
            int notFinalizedCount,
            boolean detailHidden,        // true nếu chương trình < 3 người: chỉ hiện số lượng
            Double avgTechnical,         // nhóm kỹ năng
            Double avgAttitude,          // nhóm thái độ
            Double avgFinal,             // điểm chung
            int[] distribution,          // 4 khoảng: 1.0-1.9, 2.0-2.9, 3.0-3.9, 4.0-5.0 (null nếu ẩn)
            Double avgAttendanceRate,    // tỷ lệ chuyên cần trung bình (%)
            int weeklySubmitted,
            Integer weeklyLate,
            List<DepartmentStat> departments) {}

    public record DepartmentStat(
            String departmentName,
            int internCount,
            int finalizedCount,
            boolean hidden,              // true nếu phòng ban < 3 người: ẩn điểm, ghi chú "ẩn vì nhóm quá nhỏ"
            Double avgTechnical,
            Double avgAttitude,
            Double avgFinal) {}

    /** Báo cáo cho trường: mỗi trường một mục, chỉ chứa thực tập sinh của trường đó. */
    public record School(
            String schoolName,           // "Chưa có thông tin trường" nếu hồ sơ không có
            boolean noSchoolInfo,
            List<InternRow> interns) {}

    public record InternRow(
            String fullName,
            String programName,
            String departmentName,
            String startDate,
            String endDate,
            boolean hasEvaluation,       // false => ghi "chưa có đánh giá"
            Double technicalScore,
            Double attitudeScore,
            Double punctualityScore,
            Double finalScore,
            String finalGrade,
            String comments,
            Integer attendedDays,
            Integer expectedDays) {}
}