package com.example.internmanagement.model;

public class ReportSendLog {
    public static final String PENDING = "PENDING";
    public static final String SENT = "SENT";
    public static final String FAILED = "FAILED";

    private long id;
    private long snapshotId;
    private String batchToken;
    private String schoolName;
    private String recipientEmail;
    private String status;
    private int attempts;
    private String errorMessage;
    private String sentByName;
    private String sentAt;
    private String createdAt;

    public ReportSendLog() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(long snapshotId) { this.snapshotId = snapshotId; }
    public String getBatchToken() { return batchToken; }
    public void setBatchToken(String batchToken) { this.batchToken = batchToken; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getSentByName() { return sentByName; }
    public void setSentByName(String sentByName) { this.sentByName = sentByName; }
    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}