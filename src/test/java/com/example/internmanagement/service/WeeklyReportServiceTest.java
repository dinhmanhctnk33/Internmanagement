package com.example.internmanagement.service;

import com.example.internmanagement.dao.IInternshipDAO;
import com.example.internmanagement.dao.IWeeklyReportDAO;
import com.example.internmanagement.exception.BusinessException;
import com.example.internmanagement.model.InternProfile;
import com.example.internmanagement.model.ReportAuditLog;
import com.example.internmanagement.model.ReportStatus;
import com.example.internmanagement.model.WeeklyReport;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeeklyReportServiceTest {

    @Test
    void saveDraftCreatesDraftForActiveIntern() {
        InMemoryWeeklyReportDAO reportDAO = new InMemoryWeeklyReportDAO();
        WeeklyReportService service = service(reportDAO, activeInternship());
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        WeeklyReport report = service.saveDraft("intern-1", monday, monday.plusDays(6),
                "  Tasks  ", " Results ", null, null);

        assertEquals(ReportStatus.DRAFT, report.getStatus());
        assertEquals("intern-1", report.getInternId());
        assertEquals("  Tasks  ", report.getTasksDone());
        assertEquals(1, reportDAO.reports.size());
        assertTrue(reportDAO.auditLogs.isEmpty());
    }

    @Test
    void submitMarksLateAndWritesAuditLog() {
        InMemoryWeeklyReportDAO reportDAO = new InMemoryWeeklyReportDAO();
        WeeklyReport report = draftReport("report-1", "intern-1", LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1));
        reportDAO.insert(report);
        WeeklyReportService service = service(reportDAO, activeInternship());

        WeeklyReport submitted = service.submitReport("intern-1", "report-1",
                " Completed work ", " Results ", "  ", null);

        assertEquals(ReportStatus.SUBMITTED, submitted.getStatus());
        assertTrue(submitted.isLate());
        assertFalse(submitted.isResubmitted());
        assertEquals("Completed work", submitted.getTasksDone());
        assertEquals(1, reportDAO.auditLogs.size());
        assertEquals("SUBMIT", reportDAO.auditLogs.get(0).getAction());
    }

    @Test
    void resubmissionPreservesFirstSubmissionTimeAndWritesResubmitAction() {
        InMemoryWeeklyReportDAO reportDAO = new InMemoryWeeklyReportDAO();
        WeeklyReport report = draftReport("report-2", "intern-1", LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1));
        LocalDateTime firstSubmittedAt = LocalDateTime.now().minusDays(8);
        report.setFirstSubmittedAt(firstSubmittedAt);
        report.setLate(true);
        reportDAO.insert(report);
        WeeklyReportService service = service(reportDAO, activeInternship());

        WeeklyReport submitted = service.submitReport("intern-1", "report-2",
                "Completed work", "Results", null, null);

        assertTrue(submitted.isResubmitted());
        assertTrue(submitted.isLate());
        assertEquals(firstSubmittedAt, submitted.getFirstSubmittedAt());
        assertEquals("RESUBMIT", reportDAO.auditLogs.get(0).getAction());
    }

    @Test
    void submitRejectsAnotherInternsReport() {
        InMemoryWeeklyReportDAO reportDAO = new InMemoryWeeklyReportDAO();
        reportDAO.insert(draftReport("report-3", "intern-2", LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))));
        WeeklyReportService service = service(reportDAO, activeInternship());

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.submitReport("intern-1", "report-3", "Completed work", "Results", null, null));
        assertEquals("Báo cáo không tồn tại hoặc bạn không có quyền truy cập.", exception.getMessage());
        assertTrue(reportDAO.auditLogs.isEmpty());
    }

    private WeeklyReportService service(InMemoryWeeklyReportDAO reportDAO, InternProfile internship) {
        IInternshipDAO internshipDAO = internId -> "intern-1".equals(internId) ? internship : null;
        return new WeeklyReportService(reportDAO, internshipDAO);
    }

    private InternProfile activeInternship() {
        InternProfile profile = new InternProfile();
        profile.setInternshipStatus("ACTIVE");
        profile.setStartDate(LocalDate.now().minusMonths(2));
        profile.setEndDate(LocalDate.now().plusMonths(2));
        return profile;
    }

    private WeeklyReport draftReport(String reportId, String internId, LocalDate weekStart) {
        WeeklyReport report = new WeeklyReport();
        report.setId(reportId);
        report.setInternId(internId);
        report.setWeekStartDate(weekStart);
        report.setWeekEndDate(weekStart.plusDays(6));
        report.setStatus(ReportStatus.DRAFT);
        return report;
    }

    private static class InMemoryWeeklyReportDAO implements IWeeklyReportDAO {
        private final Map<String, WeeklyReport> reports = new HashMap<>();
        private final List<ReportAuditLog> auditLogs = new ArrayList<>();

        @Override
        public WeeklyReport findByInternAndWeek(String internId, LocalDate weekStartDate) {
            return reports.values().stream()
                    .filter(report -> Objects.equals(report.getInternId(), internId)
                            && Objects.equals(report.getWeekStartDate(), weekStartDate))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public WeeklyReport findById(String reportId) {
            return reports.get(reportId);
        }

        @Override
        public void insert(WeeklyReport report) {
            reports.put(report.getId(), report);
        }

        @Override
        public void update(WeeklyReport report) {
            reports.put(report.getId(), report);
        }

        @Override
        public void insertAuditLog(ReportAuditLog auditLog) {
            auditLogs.add(auditLog);
        }
    }
}