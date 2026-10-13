package com.example.internmanagement.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Thực thể: Hợp đồng thực tập điện tử (bảng contracts)
 * Theo "Phân tích nghiệp vụ: Thực tập sinh xác nhận hợp đồng":
 * - Nhãn trạng thái: UNCONFIRMED (Chưa xác nhận) / CONFIRMED (Đã xác nhận) / AMENDMENT_REQUESTED (Yêu cầu chỉnh)
 * - "Xác nhận" là ghi nhận sự đồng ý, KHÔNG phải chữ ký pháp lý.
 * - fileVersion: tăng mỗi khi HR xóa & tải lại file mới, dùng để phát hiện
 *   trường hợp thực tập sinh đang xem bản cũ thì bị thay file giữa chừng.
 *
 * Không dùng Lombok (để tránh phụ thuộc thư viện ngoài project chưa chắc có sẵn) -
 * viết tay getter/setter như các Model khác trong project (User, Candidate...).
 */
public class Contract {
    private Long id;
    private String contractNumber;
    private Long internProfileId;
    private Date effectiveDate;
    private Date expirationDate;
    private BigDecimal monthlyAllowance;
    private String contractPdfUrl;
    private Integer fileVersion;

    private String status;              // UNCONFIRMED | CONFIRMED | AMENDMENT_REQUESTED
    private Timestamp viewedAt;
    private Long confirmedByUserId;
    private String confirmSource;       // INTERN | HR
    private Timestamp confirmedAt;

    private String amendmentReason;
    private Timestamp amendmentRequestedAt;

    public Contract() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }

    public Long getInternProfileId() { return internProfileId; }
    public void setInternProfileId(Long internProfileId) { this.internProfileId = internProfileId; }

    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }

    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }

    public BigDecimal getMonthlyAllowance() { return monthlyAllowance; }
    public void setMonthlyAllowance(BigDecimal monthlyAllowance) { this.monthlyAllowance = monthlyAllowance; }

    public String getContractPdfUrl() { return contractPdfUrl; }
    public void setContractPdfUrl(String contractPdfUrl) { this.contractPdfUrl = contractPdfUrl; }

    public Integer getFileVersion() { return fileVersion; }
    public void setFileVersion(Integer fileVersion) { this.fileVersion = fileVersion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getViewedAt() { return viewedAt; }
    public void setViewedAt(Timestamp viewedAt) { this.viewedAt = viewedAt; }

    public Long getConfirmedByUserId() { return confirmedByUserId; }
    public void setConfirmedByUserId(Long confirmedByUserId) { this.confirmedByUserId = confirmedByUserId; }

    public String getConfirmSource() { return confirmSource; }
    public void setConfirmSource(String confirmSource) { this.confirmSource = confirmSource; }

    public Timestamp getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Timestamp confirmedAt) { this.confirmedAt = confirmedAt; }

    public String getAmendmentReason() { return amendmentReason; }
    public void setAmendmentReason(String amendmentReason) { this.amendmentReason = amendmentReason; }

    public Timestamp getAmendmentRequestedAt() { return amendmentRequestedAt; }
    public void setAmendmentRequestedAt(Timestamp amendmentRequestedAt) { this.amendmentRequestedAt = amendmentRequestedAt; }
}
