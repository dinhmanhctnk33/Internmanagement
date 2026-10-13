package com.example.internmanagement.model;

public class ReportSnapshot {
    public static final String TYPE_LEADER = "LEADER";
    public static final String TYPE_SCHOOL = "SCHOOL";

    private long id;
    private String reportType;     // LEADER hoặc SCHOOL
    private String programIds;     // ví dụ "1,3,5" (đã sắp xếp)
    private String programNames;
    private String payloadJson;    // nội dung báo cáo, bất biến
    private Long createdBy;
    private String createdByName;
    private String createdAt;
    private boolean newerExists;   // có bản mới hơn cùng loại và cùng phạm vi

    public ReportSnapshot() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getProgramIds() { return programIds; }
    public void setProgramIds(String programIds) { this.programIds = programIds; }
    public String getProgramNames() { return programNames; }
    public void setProgramNames(String programNames) { this.programNames = programNames; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public boolean isNewerExists() { return newerExists; }
    public void setNewerExists(boolean newerExists) { this.newerExists = newerExists; }

    public boolean isLeaderReport() { return TYPE_LEADER.equals(reportType); }
}