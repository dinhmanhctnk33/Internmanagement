package com.example.internmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WeeklyReport {
    private String id;
    private String internId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String tasksDone;
    private String results;
    private String issues;
    private String nextWeekPlans;
    private ReportStatus status;
    private boolean isLate;
    private boolean isResubmitted;
    private LocalDateTime firstSubmittedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime reopenedAt;
    private String reopenedReason;
    private String mentorFeedback;
    private String mentorId;
    private LocalDateTime feedbackCreatedAt;

    public WeeklyReport() {
        this.status = ReportStatus.DRAFT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInternId() {
        return internId;
    }

    public void setInternId(String internId) {
        this.internId = internId;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }

    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public String getTasksDone() {
        return tasksDone;
    }

    public void setTasksDone(String tasksDone) {
        this.tasksDone = tasksDone;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getIssues() {
        return issues;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }

    public String getNextWeekPlans() {
        return nextWeekPlans;
    }

    public void setNextWeekPlans(String nextWeekPlans) {
        this.nextWeekPlans = nextWeekPlans;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public boolean isLate() {
        return isLate;
    }

    public void setLate(boolean late) {
        isLate = late;
    }

    public boolean isResubmitted() {
        return isResubmitted;
    }

    public void setResubmitted(boolean resubmitted) {
        isResubmitted = resubmitted;
    }

    public LocalDateTime getFirstSubmittedAt() {
        return firstSubmittedAt;
    }

    public void setFirstSubmittedAt(LocalDateTime firstSubmittedAt) {
        this.firstSubmittedAt = firstSubmittedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReopenedAt() {
        return reopenedAt;
    }

    public void setReopenedAt(LocalDateTime reopenedAt) {
        this.reopenedAt = reopenedAt;
    }

    public String getReopenedReason() {
        return reopenedReason;
    }

    public void setReopenedReason(String reopenedReason) {
        this.reopenedReason = reopenedReason;
    }

    public String getMentorFeedback() {
        return mentorFeedback;
    }

    public void setMentorFeedback(String mentorFeedback) {
        this.mentorFeedback = mentorFeedback;
    }

    public String getMentorId() {
        return mentorId;
    }

    public void setMentorId(String mentorId) {
        this.mentorId = mentorId;
    }

    public LocalDateTime getFeedbackCreatedAt() {
        return feedbackCreatedAt;
    }

    public void setFeedbackCreatedAt(LocalDateTime feedbackCreatedAt) {
        this.feedbackCreatedAt = feedbackCreatedAt;
    }
}