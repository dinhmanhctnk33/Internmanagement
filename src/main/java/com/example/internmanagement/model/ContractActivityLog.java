package com.example.internmanagement.model;

import java.sql.Timestamp;

/**
 * Thực thể: Nhật ký hoạt động trên hợp đồng (bảng contract_activity_logs)
 * Ghi lại đúng yêu cầu: "Nhật ký ghi mọi bước: xác nhận (kèm nguồn),
 * yêu cầu chỉnh, đóng yêu cầu." Bảng này độc lập với contracts (không
 * CASCADE DELETE) để giữ lại lịch sử khi HR xóa hợp đồng.
 */
public class ContractActivityLog {
    private Long id;
    private Long contractId;
    private String actionType;     // CONFIRMED | AMENDMENT_REQUESTED | AMENDMENT_CLOSED
                                    // | HR_REUPLOADED | HR_MANUAL_CONFIRMED | DATES_EDITED
    private Long actorUserId;
    private String actorSource;    // INTERN | HR
    private Integer fileVersion;
    private String reason;
    private String oldValueJson;
    private String newValueJson;
    private Timestamp createdAt;

    public ContractActivityLog() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }

    public String getActorSource() { return actorSource; }
    public void setActorSource(String actorSource) { this.actorSource = actorSource; }

    public Integer getFileVersion() { return fileVersion; }
    public void setFileVersion(Integer fileVersion) { this.fileVersion = fileVersion; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getOldValueJson() { return oldValueJson; }
    public void setOldValueJson(String oldValueJson) { this.oldValueJson = oldValueJson; }

    public String getNewValueJson() { return newValueJson; }
    public void setNewValueJson(String newValueJson) { this.newValueJson = newValueJson; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
