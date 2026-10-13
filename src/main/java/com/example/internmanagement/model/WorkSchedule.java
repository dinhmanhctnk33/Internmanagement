package com.example.internmanagement.model;

public class WorkSchedule {
    private int id;
    private int groupId;
    private String groupName;
    private String shiftName;  // Ví dụ: Ca sáng, Ca chiều, Linh hoạt
    private String startTime;  // HH:mm
    private String endTime;    // HH:mm
    private String workDays;   // Thứ 2 đến Thứ 6
    private String note;

    public WorkSchedule() {}

    public WorkSchedule(int id, int groupId, String groupName, String shiftName, String startTime, String endTime, String workDays, String note) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workDays = workDays;
        this.note = note;
    }

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

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