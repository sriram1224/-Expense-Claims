package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.DuplicateAlert;
import com.vsp.expenseclaims.entity.enums.DuplicateStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DuplicateAlertResponse {

    private Long id;
    private Long expenseItemId;
    private String expenseItemMerchant;
    private BigDecimal expenseItemAmount;
    private String expenseItemDate;
    private Long expenseItemClaimId;
    private String expenseItemEmployee;

    private Long matchedExpenseItemId;
    private String matchedExpenseItemMerchant;
    private BigDecimal matchedExpenseItemAmount;
    private String matchedExpenseItemDate;
    private Long matchedExpenseItemClaimId;
    private String matchedExpenseItemEmployee;

    // Component scores per Adjustment #3
    private BigDecimal amountScore;
    private BigDecimal merchantScore;
    private BigDecimal dateScore;
    private BigDecimal similarityScore;

    private DuplicateStatus status;
    private LocalDateTime detectedAt;

    public DuplicateAlertResponse() {}

    public DuplicateAlertResponse(DuplicateAlert alert) {
        this.id = alert.getId();
        if (alert.getExpenseItem() != null) {
            this.expenseItemId = alert.getExpenseItem().getId();
            this.expenseItemMerchant = alert.getExpenseItem().getMerchantName();
            this.expenseItemAmount = alert.getExpenseItem().getAmount();
            this.expenseItemDate = alert.getExpenseItem().getExpenseDate().toString();
            if (alert.getExpenseItem().getClaim() != null) {
                this.expenseItemClaimId = alert.getExpenseItem().getClaim().getId();
                if (alert.getExpenseItem().getClaim().getEmployee() != null) {
                    this.expenseItemEmployee = alert.getExpenseItem().getClaim().getEmployee().getFullName();
                }
            }
        }
        if (alert.getMatchedExpenseItem() != null) {
            this.matchedExpenseItemId = alert.getMatchedExpenseItem().getId();
            this.matchedExpenseItemMerchant = alert.getMatchedExpenseItem().getMerchantName();
            this.matchedExpenseItemAmount = alert.getMatchedExpenseItem().getAmount();
            this.matchedExpenseItemDate = alert.getMatchedExpenseItem().getExpenseDate().toString();
            if (alert.getMatchedExpenseItem().getClaim() != null) {
                this.matchedExpenseItemClaimId = alert.getMatchedExpenseItem().getClaim().getId();
                if (alert.getMatchedExpenseItem().getClaim().getEmployee() != null) {
                    this.matchedExpenseItemEmployee = alert.getMatchedExpenseItem().getClaim().getEmployee().getFullName();
                }
            }
        }
        this.amountScore = alert.getAmountScore();
        this.merchantScore = alert.getMerchantScore();
        this.dateScore = alert.getDateScore();
        this.similarityScore = alert.getSimilarityScore();
        this.status = alert.getStatus();
        this.detectedAt = alert.getDetectedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExpenseItemId() { return expenseItemId; }
    public void setExpenseItemId(Long expenseItemId) { this.expenseItemId = expenseItemId; }

    public String getExpenseItemMerchant() { return expenseItemMerchant; }
    public void setExpenseItemMerchant(String expenseItemMerchant) { this.expenseItemMerchant = expenseItemMerchant; }

    public BigDecimal getExpenseItemAmount() { return expenseItemAmount; }
    public void setExpenseItemAmount(BigDecimal expenseItemAmount) { this.expenseItemAmount = expenseItemAmount; }

    public String getExpenseItemDate() { return expenseItemDate; }
    public void setExpenseItemDate(String expenseItemDate) { this.expenseItemDate = expenseItemDate; }

    public Long getExpenseItemClaimId() { return expenseItemClaimId; }
    public void setExpenseItemClaimId(Long expenseItemClaimId) { this.expenseItemClaimId = expenseItemClaimId; }

    public String getExpenseItemEmployee() { return expenseItemEmployee; }
    public void setExpenseItemEmployee(String expenseItemEmployee) { this.expenseItemEmployee = expenseItemEmployee; }

    public Long getMatchedExpenseItemId() { return matchedExpenseItemId; }
    public void setMatchedExpenseItemId(Long matchedExpenseItemId) { this.matchedExpenseItemId = matchedExpenseItemId; }

    public String getMatchedExpenseItemMerchant() { return matchedExpenseItemMerchant; }
    public void setMatchedExpenseItemMerchant(String matchedExpenseItemMerchant) { this.matchedExpenseItemMerchant = matchedExpenseItemMerchant; }

    public BigDecimal getMatchedExpenseItemAmount() { return matchedExpenseItemAmount; }
    public void setMatchedExpenseItemAmount(BigDecimal matchedExpenseItemAmount) { this.matchedExpenseItemAmount = matchedExpenseItemAmount; }

    public String getMatchedExpenseItemDate() { return matchedExpenseItemDate; }
    public void setMatchedExpenseItemDate(String matchedExpenseItemDate) { this.matchedExpenseItemDate = matchedExpenseItemDate; }

    public Long getMatchedExpenseItemClaimId() { return matchedExpenseItemClaimId; }
    public void setMatchedExpenseItemClaimId(Long matchedExpenseItemClaimId) { this.matchedExpenseItemClaimId = matchedExpenseItemClaimId; }

    public String getMatchedExpenseItemEmployee() { return matchedExpenseItemEmployee; }
    public void setMatchedExpenseItemEmployee(String matchedExpenseItemEmployee) { this.matchedExpenseItemEmployee = matchedExpenseItemEmployee; }

    public BigDecimal getAmountScore() { return amountScore; }
    public void setAmountScore(BigDecimal amountScore) { this.amountScore = amountScore; }

    public BigDecimal getMerchantScore() { return merchantScore; }
    public void setMerchantScore(BigDecimal merchantScore) { this.merchantScore = merchantScore; }

    public BigDecimal getDateScore() { return dateScore; }
    public void setDateScore(BigDecimal dateScore) { this.dateScore = dateScore; }

    public BigDecimal getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(BigDecimal similarityScore) { this.similarityScore = similarityScore; }

    public DuplicateStatus getStatus() { return status; }
    public void setStatus(DuplicateStatus status) { this.status = status; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
}
