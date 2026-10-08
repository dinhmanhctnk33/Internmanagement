package com.example.internmanagement.model;
import java.time.LocalDateTime;
 
public class Candidate {
 
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String universityName;
    private String majorName;
    private String desiredPosition;
    private String cvFileUrl;
    private String applicationStatus;
    private String hrNotes;
    private LocalDateTime appliedAt;
 
    public Candidate() {
    }
 
    public Candidate(
            int id,
            String fullName,
            String email,
            String phone,
            String universityName,
            String majorName,
            String desiredPosition,
            String cvFileUrl,
            String applicationStatus,
            String hrNotes,
            LocalDateTime appliedAt
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.universityName = universityName;
        this.majorName = majorName;
        this.desiredPosition = desiredPosition;
        this.cvFileUrl = cvFileUrl;
        this.applicationStatus = applicationStatus;
        this.hrNotes = hrNotes;
        this.appliedAt = appliedAt;
    }
 
    public int getId() {
        return id;
    }
 
    public void setId(int id) {
        this.id = id;
    }
 
    public String getFullname() {
        return fullName;
    }
 
    public void setFullname(String fullName) {
        this.fullName = fullName;
    }
 
    public String getEmail() {
        return email;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }
 
    public String getPhone() {
        return phone;
    }
 
    public void setPhone(String phone) {
        this.phone = phone;
    }
 
    public String getUniversityname() {
        return universityName;
    }
 
    public void setUniversityname(String universityName) {
        this.universityName = universityName;
    }
 
    public String getMajorname() {
        return majorName;
    }
 
    public void setMajorname(String majorName) {
        this.majorName = majorName;
    }
 
    public String getDesiredposition() {
        return desiredPosition;
    }
 
    public void setDesiredposition(String desiredPosition) {
        this.desiredPosition = desiredPosition;
    }
 
    public String getCvfileurl() {
        return cvFileUrl;
    }
 
    public void setCvfileurl(String cvFileUrl) {
        this.cvFileUrl = cvFileUrl;
    }
 
    public String getApplicationstatus() {
        return applicationStatus;
    }
 
    public void setApplicationstatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }
 
    public String getHrnotes() {
        return hrNotes;
    }
 
    public void setHrnotes(String hrNotes) {
        this.hrNotes = hrNotes;
    }
 
    public LocalDateTime getAppliedat() {
        return appliedAt;
    }
 
    public void setAppliedat(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
