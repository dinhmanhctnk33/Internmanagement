package com.example.internmanagement.model;

import java.time.LocalDateTime;

public class ReportAuditLog {
    private String id;
    private String reportId;
    private String performedBy;
    private String action;
    private LocalDateTime timestamp;
    private String details;

    public ReportAuditLog() {
    }

    public ReportAuditLog(String reportId, String performedBy, String action,
                          LocalDateTime timestamp, String details) {
        this.reportId = reportId;
        this.performedBy = performedBy;
        this.action = action;
        this.timestamp = timestamp;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}