package com.example.internmanagement.model;

public class WorkSchedule {
    private long id;
    private long groupId;
    private String groupName;
    private String shiftName;
    private String startTime;
    private String endTime;
    private String workDays;
    private String note;

    public WorkSchedule() {}

    public WorkSchedule(long id, long groupId, String groupName, String shiftName,
                        String startTime, String endTime, String workDays, String note) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workDays = workDays;
        this.note = note;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getGroupId() { return groupId; }
    public void setGroupId(long groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getWorkDays() { return workDays; }
    public void setWorkDays(String workDays) { this.workDays = workDays; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
