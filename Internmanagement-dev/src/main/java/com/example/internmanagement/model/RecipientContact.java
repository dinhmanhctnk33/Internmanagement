package com.example.internmanagement.model;

public class RecipientContact {
    public static final String TYPE_SCHOOL = "SCHOOL";
    public static final String TYPE_LEADER = "LEADER";
    public static final String LEADER_ORG = "Ban lãnh đạo";

    private long id;
    private String recipientType;     // SCHOOL hoặc LEADER
    private String organizationName;  // tên trường, hoặc "Ban lãnh đạo"
    private String contactName;
    private String email;
    private String createdAt;

    public RecipientContact() {}

    public RecipientContact(long id, String recipientType, String organizationName,
                            String contactName, String email, String createdAt) {
        this.id = id;
        this.recipientType = recipientType;
        this.organizationName = organizationName;
        this.contactName = contactName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}