package com.example.internmanagement.service;

import com.example.internmanagement.dao.IInternshipDAO;
import com.example.internmanagement.dao.IWeeklyReportDAO;
import com.example.internmanagement.exception.BusinessException;
import com.example.internmanagement.model.InternProfile;
import com.example.internmanagement.model.ReportAuditLog;
import com.example.internmanagement.model.ReportStatus;
import com.example.internmanagement.model.WeeklyReport;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class WeeklyReportService {
    private static final int MAX_LENGTH = 4000;
    private static final DateTimeFormatter SUBMITTED_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final IWeeklyReportDAO reportDAO;
    private final IInternshipDAO internshipDAO;

    public WeeklyReportService(IWeeklyReportDAO reportDAO, IInternshipDAO internshipDAO) {
        this.reportDAO = reportDAO;
        this.internshipDAO = internshipDAO;
    }

    public synchronized WeeklyReport saveDraft(String internId, LocalDate weekStartDate, LocalDate weekEndDate,
                                               String tasksDone, String results, String issues, String nextWeekPlans) {
        validateWeekRange(weekStartDate, weekEndDate);
        InternProfile internship = getEligibleInternship(internId);
        LocalDate today = LocalDate.now();

        if (today.isBefore(weekStartDate)) {
            throw new BusinessException("Không thể soạn báo cáo cho tuần chưa bắt đầu.");
        }
        if (internship.getStartDate() != null && weekEndDate.isBefore(internship.getStartDate())
                || internship.getEndDate() != null && weekStartDate.isAfter(internship.getEndDate())) {
            throw new BusinessException("Tuần báo cáo nằm ngoài thời gian kỳ thực tập.");
        }

        WeeklyReport report = reportDAO.findByInternAndWeek(internId, weekStartDate);
        if (report != null && report.getStatus() == ReportStatus.SUBMITTED) {
            throw new BusinessException("Báo cáo đã được nộp và khóa. Không thể lưu nháp lại.");
        }

        boolean isNewReport = report == null;
        if (report == null) {
            report = new WeeklyReport();
            report.setId(UUID.randomUUID().toString());
            report.setInternId(internId);
            report.setWeekStartDate(weekStartDate);
            report.setWeekEndDate(weekEndDate);
        }
        report.setTasksDone(tasksDone);
        report.setResults(results);
        report.setIssues(issues);
        report.setNextWeekPlans(nextWeekPlans);

        if (isNewReport) {
            reportDAO.insert(report);
        } else {
            reportDAO.update(report);
        }
        return report;
    }

    public synchronized WeeklyReport submitReport(String internId, String reportId,
                                                  String tasksDone, String results, String issues,
                                                  String nextWeekPlans) {
        LocalDateTime serverNow = LocalDateTime.now();
        WeeklyReport report = reportDAO.findById(reportId);
        if (report == null || !internId.equals(report.getInternId())) {
            throw new BusinessException("Báo cáo không tồn tại hoặc bạn không có quyền truy cập.");
        }

        if (report.getStatus() == ReportStatus.SUBMITTED) {
            String time = report.getSubmittedAt() == null
                    ? ""
                    : report.getSubmittedAt().format(SUBMITTED_TIME_FORMAT);
            throw new BusinessException("Báo cáo đã được nộp lúc " + time + ".");
        }

        InternProfile internship = getEligibleInternship(internId);
        validateReportFields(tasksDone, results, issues, nextWeekPlans);
        if (report.getWeekEndDate() == null || internship.getEndDate() == null) {
            throw new BusinessException("Không đủ thông tin ngày để xác định hạn nộp báo cáo.");
        }

        report.setTasksDone(tasksDone.trim());
        report.setResults(results.trim());
        report.setIssues(normalizeOptionalField(issues));
        report.setNextWeekPlans(normalizeOptionalField(nextWeekPlans));

        LocalDateTime deadline = LocalDateTime.of(report.getWeekEndDate(), LocalTime.MAX);
        LocalDateTime internshipEnd = LocalDateTime.of(internship.getEndDate(), LocalTime.MAX);
        if (internshipEnd.isBefore(deadline)) {
            deadline = internshipEnd;
        }
        if (serverNow.isAfter(internshipEnd)) {
            throw new BusinessException("Kỳ thực tập đã kết thúc, không thể nộp báo cáo.");
        }

        boolean isFirstSubmission = report.getFirstSubmittedAt() == null;
        if (isFirstSubmission) {
            report.setFirstSubmittedAt(serverNow);
            report.setLate(serverNow.isAfter(deadline));
        } else {
            report.setResubmitted(true);
        }

        report.setStatus(ReportStatus.SUBMITTED);
        report.setSubmittedAt(serverNow);
        reportDAO.update(report);

        String action = isFirstSubmission ? "SUBMIT" : "RESUBMIT";
        ReportAuditLog auditLog = new ReportAuditLog(
                report.getId(),
                internId,
                action,
                serverNow,
                "Nộp báo cáo tuần. Nộp muộn: " + report.isLate()
                        + ", Nộp lại: " + report.isResubmitted());
        reportDAO.insertAuditLog(auditLog);
        return report;
    }

    private InternProfile getEligibleInternship(String internId) {
        InternProfile internship = internshipDAO.getInternshipByInternId(internId);
        String status = internship == null ? null : internship.getInternshipStatus();
        if (!"ACTIVE".equalsIgnoreCase(status) && !"ACCEPTED".equalsIgnoreCase(status)) {
            throw new BusinessException("Chỉ thực tập sinh đang được nhận mới có quyền soạn hoặc nộp báo cáo.");
        }
        return internship;
    }

    private void validateWeekRange(LocalDate weekStartDate, LocalDate weekEndDate) {
        if (weekStartDate == null || weekEndDate == null
                || weekStartDate.getDayOfWeek() != DayOfWeek.MONDAY
                || !weekEndDate.equals(weekStartDate.plusDays(6))) {
            throw new BusinessException("Tuần báo cáo phải bắt đầu vào Thứ Hai và kết thúc vào Chủ Nhật.");
        }
    }

    private void validateReportFields(String tasksDone, String results, String issues, String nextWeekPlans) {
        validateRequiredField(tasksDone, "Việc đã làm");
        validateRequiredField(results, "Kết quả");
        validateMaximumLength(tasksDone, "Việc đã làm");
        validateMaximumLength(results, "Kết quả");
        validateMaximumLength(issues, "Khó khăn");
        validateMaximumLength(nextWeekPlans, "Kế hoạch tuần sau");
    }

    private void validateRequiredField(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Trường '" + fieldName + "' là bắt buộc.");
        }
    }

    private void validateMaximumLength(String value, String fieldName) {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new BusinessException("Phần '" + fieldName + "' vượt quá " + MAX_LENGTH + " ký tự.");
        }
    }

    private String normalizeOptionalField(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}