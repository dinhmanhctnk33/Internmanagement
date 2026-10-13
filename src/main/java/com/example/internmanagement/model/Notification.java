package com.example.internmanagement.model;

import java.time.LocalDateTime;

public class Notification {
    private long id;
    private int userId;
    private NotificationType type;
    private String title;
    private String message;
    private String targetUrl;
    private String targetEntityType;
    private Long targetEntityId;
    private boolean read;
    private LocalDateTime createdAt;
    private String reminderKey;

    public Notification() {
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRecipientId() {
        return userId;
    }

    public void setRecipientId(int recipientId) {
        this.userId = recipientId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShortContent() {
        return message;
    }

    public void setShortContent(String shortContent) {
        this.message = shortContent;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetEntityType() {
        return targetEntityType;
    }

    public void setTargetEntityType(String targetEntityType) {
        this.targetEntityType = targetEntityType;
    }

    public Long getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(Long targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public boolean isRead() {
        return read;
    }

    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getReminderKey() {
        return reminderKey;
    }

    public void setReminderKey(String reminderKey) {
        this.reminderKey = reminderKey;
    }
}
