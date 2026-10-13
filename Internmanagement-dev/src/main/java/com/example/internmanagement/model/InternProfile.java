package com.example.internmanagement.model;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
public class InternProfile {
 
    private int id;
    private int userId;
    private String internCode;
    private String identityNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String universityName;
    private String majorName;
    private int programId;
    private int mentorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String bankAccountNo;
    private String bankName;
    private String internshipStatus;
    private String phoneNumber;
    private String overallVerificationStatus;
    private String creationSource;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String fullName;
    private String email;
    private String mentorName;
 
    public InternProfile() {
    }
 
    public InternProfile(
            int id,
            int userId,
            String internCode,
            String identityNumber,
            LocalDate dateOfBirth,
            String gender,
            String address,
            String universityName,
            String majorName,
            int programId,
            int mentorId,
            LocalDate startDate,
            LocalDate endDate,
            String bankAccountNo,
            String bankName,
            String internshipStatus,
            String phoneNumber,
            String overallVerificationStatus,
            String creationSource,
            int createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.internCode = internCode;
        this.identityNumber = identityNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.universityName = universityName;
        this.majorName = majorName;
        this.programId = programId;
        this.mentorId = mentorId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bankAccountNo = bankAccountNo;
        this.bankName = bankName;
        this.internshipStatus = internshipStatus;
        this.phoneNumber = phoneNumber;
        this.overallVerificationStatus = overallVerificationStatus;
        this.creationSource = creationSource;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
 
    public String getInternCode() {
        return internCode;
    }
 
    public void setInternCode(String internCode) {
        this.internCode = internCode;
    }
 
    public String getIdentityNumber() {
        return identityNumber;
    }
 
    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }
 
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
 
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
 
    public String getGender() {
        return gender;
    }
 
    public void setGender(String gender) {
        this.gender = gender;
    }
 
    public String getAddress() {
        return address;
    }
 
    public void setAddress(String address) {
        this.address = address;
    }
 
    public String getUniversityName() {
        return universityName;
    }
 
    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }
 
    public String getMajorName() {
        return majorName;
    }
 
    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }
 
    public int getProgramId() {
        return programId;
    }
 
    public void setProgramId(int programId) {
        this.programId = programId;
    }
 
    public int getMentorId() {
        return mentorId;
    }
 
    public void setMentorId(int mentorId) {
        this.mentorId = mentorId;
    }
 
    public LocalDate getStartDate() {
        return startDate;
    }
 
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
 
    public LocalDate getEndDate() {
        return endDate;
    }
 
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
 
    public String getBankAccountNo() {
        return bankAccountNo;
    }
 
    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }
 
    public String getBankName() {
        return bankName;
    }
 
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
 
    public String getInternshipStatus() {
        return internshipStatus;
    }
 
    public void setInternshipStatus(String internshipStatus) {
        this.internshipStatus = internshipStatus;
    }
 
    public String getPhoneNumber() {
        return phoneNumber;
    }
 
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
 
    public String getOverallVerificationStatus() {
        return overallVerificationStatus;
    }
 
    public void setOverallVerificationStatus(String overallVerificationStatus) {
        this.overallVerificationStatus = overallVerificationStatus;
    }
 
    public String getCreationSource() {
        return creationSource;
    }
 
    public void setCreationSource(String creationSource) {
        this.creationSource = creationSource;
    }
 
    public int getCreatedBy() {
        return createdBy;
    }
 
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
 
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
 
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
 
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
 
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    // Alias methods for JSP EL compatibility
    public String getUserFullName() {
        return fullName;
    }

    public String getUserEmail() {
        return email;
    }
}
