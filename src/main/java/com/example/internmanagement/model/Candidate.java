package com.example.internmanagement.model;
import java.time.LocalDateTime;
 
public class Candidate {
 
    private int id;
    private int userId;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
 
    public String getFullname() {
        return fullName;
    }
 
    public void setFullname(String fullName) {
        this.fullName = fullName;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
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

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }
 
    public String getMajorname() {
        return majorName;
    }
 
    public void setMajorname(String majorName) {
        this.majorName = majorName;
    }

    public String getDesiredPosition() {
        return desiredPosition;
    }

    public void setDesiredPosition(String desiredPosition) {
        this.desiredPosition = desiredPosition;
    }
 
    public String getDesiredposition() {
        return desiredPosition;
    }
 
    public void setDesiredposition(String desiredPosition) {
        this.desiredPosition = desiredPosition;
    }

    public String getCvFileUrl() {
        return cvFileUrl;
    }

    public void setCvFileUrl(String cvFileUrl) {
        this.cvFileUrl = cvFileUrl;
    }
 
    public String getCvfileurl() {
        return cvFileUrl;
    }
 
    public void setCvfileurl(String cvFileUrl) {
        this.cvFileUrl = cvFileUrl;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }
 
    public String getApplicationstatus() {
        return applicationStatus;
    }
 
    public void setApplicationstatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getHrNotes() {
        return hrNotes;
    }

    public void setHrNotes(String hrNotes) {
        this.hrNotes = hrNotes;
    }
 
    public String getHrnotes() {
        return hrNotes;
    }
 
    public void setHrnotes(String hrNotes) {
        this.hrNotes = hrNotes;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
 
    public LocalDateTime getAppliedat() {
        return appliedAt;
    }
 
    public void setAppliedat(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
