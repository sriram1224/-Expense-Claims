package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.enums.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ReceiptParseResponse {

    private String merchant;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private ExpenseCategory category;
    private int confidence;
    private String rawText;

    public ReceiptParseResponse() {}

    public ReceiptParseResponse(String merchant, BigDecimal amount, LocalDate expenseDate, ExpenseCategory category, int confidence, String rawText) {
        this.merchant = merchant;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.category = category;
        this.confidence = confidence;
        this.rawText = rawText;
    }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public int getConfidence() { return confidence; }
    public void setConfidence(int confidence) { this.confidence = confidence; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
