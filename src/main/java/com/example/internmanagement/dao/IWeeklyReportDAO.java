package com.example.internmanagement.dao;

import com.example.internmanagement.model.ReportAuditLog;
import com.example.internmanagement.model.WeeklyReport;

import java.time.LocalDate;

public interface IWeeklyReportDAO {

    WeeklyReport findByInternAndWeek(String internId, LocalDate weekStartDate);

    WeeklyReport findById(String reportId);

    void insert(WeeklyReport report);

    void update(WeeklyReport report);

    void insertAuditLog(ReportAuditLog auditLog);
}