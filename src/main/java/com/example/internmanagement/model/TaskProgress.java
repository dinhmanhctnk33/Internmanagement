package com.example.internmanagement.model;

import java.sql.Timestamp;

public class TaskProgress {
    private int id;
    private int internId;
    private int taskId;
    private String taskName;
    private int progressPercent;
    private String status;
    private String note;
    private Timestamp updatedAt;

    public TaskProgress() {}

    public TaskProgress(int id, int internId, int taskId, String taskName, int progressPercent, String status, String note, Timestamp updatedAt) {
        this.id = id;
        this.internId = internId;
        this.taskId = taskId;
        this.taskName = taskName;
        this.progressPercent = progressPercent;
        this.status = status;
        this.note = note;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getInternId() { return internId; }
    public void setInternId(int internId) { this.internId = internId; }
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}