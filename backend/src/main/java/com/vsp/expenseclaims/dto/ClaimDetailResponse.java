package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.Claim;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClaimDetailResponse {

    private Long claimId;
    private String claimNumber;
    private String title;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private ClaimStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private List<ExpenseItemResponse> items = new ArrayList<>();
    private List<ClaimApprovalResponse> approvals = new ArrayList<>();
    private PaymentResponse payment;

    public ClaimDetailResponse() {}

    public ClaimDetailResponse(Claim claim) {
        this.claimId = claim.getId();
        this.claimNumber = claim.getClaimNumber();
        this.title = claim.getTitle();
        if (claim.getEmployee() != null) {
            this.employeeId = claim.getEmployee().getId();
            this.employeeName = claim.getEmployee().getFullName();
            this.employeeEmail = claim.getEmployee().getEmail();
        }
        this.status = claim.getStatus();
        this.totalAmount = claim.getTotalAmount();
        this.submittedAt = claim.getSubmittedAt();
        this.approvedAt = claim.getApprovedAt();
        this.paidAt = claim.getPaidAt();
        this.createdAt = claim.getCreatedAt();

        if (claim.getExpenseItems() != null) {
            this.items = claim.getExpenseItems().stream()
                    .map(ExpenseItemResponse::new)
                    .collect(Collectors.toList());
        }
        if (claim.getApprovals() != null) {
            this.approvals = claim.getApprovals().stream()
                    .map(ClaimApprovalResponse::new)
                    .collect(Collectors.toList());
        }
        if (claim.getPayment() != null) {
            this.payment = new PaymentResponse(claim.getPayment());
        }
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

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ExpenseItemResponse> getItems() { return items; }
    public void setItems(List<ExpenseItemResponse> items) { this.items = items; }

    public List<ClaimApprovalResponse> getApprovals() { return approvals; }
    public void setApprovals(List<ClaimApprovalResponse> approvals) { this.approvals = approvals; }

    public PaymentResponse getPayment() { return payment; }
    public void setPayment(PaymentResponse payment) { this.payment = payment; }
}
