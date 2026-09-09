package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.ExpenseItem;
import com.vsp.expenseclaims.entity.enums.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseItemResponse {

    private Long id;
    private Long claimId;
    private String merchantName;
    private LocalDate expenseDate;
    private ExpenseCategory category;
    private BigDecimal amount;
    private String receiptText;
    private Boolean parsedSuccessfully;
    private LocalDateTime createdAt;
    private boolean duplicateWarning;
    private BigDecimal similarityScore;

    public ExpenseItemResponse() {}

    public ExpenseItemResponse(ExpenseItem item) {
        this.id = item.getId();
        this.claimId = item.getClaim() != null ? item.getClaim().getId() : null;
        this.merchantName = item.getMerchantName();
        this.expenseDate = item.getExpenseDate();
        this.category = item.getCategory();
        this.amount = item.getAmount();
        this.receiptText = item.getReceiptText();
        this.parsedSuccessfully = item.getParsedSuccessfully();
        this.createdAt = item.getCreatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReceiptText() { return receiptText; }
    public void setReceiptText(String receiptText) { this.receiptText = receiptText; }

    public Boolean getParsedSuccessfully() { return parsedSuccessfully; }
    public void setParsedSuccessfully(Boolean parsedSuccessfully) { this.parsedSuccessfully = parsedSuccessfully; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isDuplicateWarning() { return duplicateWarning; }
    public void setDuplicateWarning(boolean duplicateWarning) { this.duplicateWarning = duplicateWarning; }

    public BigDecimal getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(BigDecimal similarityScore) { this.similarityScore = similarityScore; }
}
