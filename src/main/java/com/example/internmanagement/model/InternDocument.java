package com.example.internmanagement.model;
import java.time.LocalDateTime;
 
public class InternDocument {
 
    private int id;
    private int internProfileId;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private Long fileSizeBytes;
    private String approvalStatus;
    private String rejectionNote;
    private LocalDateTime uploadedAt;
    private Integer version;
    private Long reviewerId;
    private LocalDateTime reviewedAt;
    private Boolean isLatest;
 
    public InternDocument() {
    }
 
    public InternDocument(
            int id,
            int internProfileId,
            String documentType,
            String fileName,
            String fileUrl,
            Long fileSizeBytes,
            String approvalStatus,
            String rejectionNote,
            LocalDateTime uploadedAt,
            Integer version,
            Long reviewerId,
            LocalDateTime reviewedAt,
            Boolean isLatest
    ) {
        this.id = id;
        this.internProfileId = internProfileId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileSizeBytes = fileSizeBytes;
        this.approvalStatus = approvalStatus;
        this.rejectionNote = rejectionNote;
        this.uploadedAt = uploadedAt;
        this.version = version;
        this.reviewerId = reviewerId;
        this.reviewedAt = reviewedAt;
        this.isLatest = isLatest;
    }
 
    public int getId() {
        return id;
    }
 
    public void setId(int id) {
        this.id = id;
    }
 
    public int getInternProfileId() {
        return internProfileId;
    }
 
    public void setInternProfileId(int internProfileId) {
        this.internProfileId = internProfileId;
    }
 
    public String getDocumentType() {
        return documentType;
    }
 
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
 
    public String getFileName() {
        return fileName;
    }
 
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
 
    public String getFileUrl() {
        return fileUrl;
    }
 
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
 
    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }
 
    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }
 
    public String getApprovalStatus() {
        return approvalStatus;
    }
 
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
 
    public String getRejectionNote() {
        return rejectionNote;
    }
 
    public void setRejectionNote(String rejectionNote) {
        this.rejectionNote = rejectionNote;
    }
 
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
 
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
 
    public Integer getVersion() {
        return version;
    }
 
    public void setVersion(Integer version) {
        this.version = version;
    }
 
    public Long getReviewerId() {
        return reviewerId;
    }
 
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }
 
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
 
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
 
    public Boolean getIsLatest() {
        return isLatest;
    }
 
    public void setIsLatest(Boolean isLatest) {
        this.isLatest = isLatest;
    }
}

