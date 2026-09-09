package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.Claim;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ClaimResponse {

    private Long claimId;
    private String claimNumber;
    private String title;
    private Long employeeId;
    private String employeeName;
    private ClaimStatus status;
    private BigDecimal totalAmount;
    private int itemCount;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public ClaimResponse() {}

    public ClaimResponse(Claim claim) {
        this.claimId = claim.getId();
        this.claimNumber = claim.getClaimNumber();
        this.title = claim.getTitle();
        if (claim.getEmployee() != null) {
            this.employeeId = claim.getEmployee().getId();
            this.employeeName = claim.getEmployee().getFullName();
        }
        this.status = claim.getStatus();
        this.totalAmount = claim.getTotalAmount();
        this.itemCount = claim.getExpenseItems() != null ? claim.getExpenseItems().size() : 0;
        this.submittedAt = claim.getSubmittedAt();
        this.approvedAt = claim.getApprovedAt();
        this.paidAt = claim.getPaidAt();
        this.createdAt = claim.getCreatedAt();
    }

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
