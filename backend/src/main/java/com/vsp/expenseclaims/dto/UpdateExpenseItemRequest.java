package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.enums.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateExpenseItemRequest {

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    private String receiptText;

    public UpdateExpenseItemRequest() {}

    public UpdateExpenseItemRequest(String merchantName, BigDecimal amount, LocalDate expenseDate, ExpenseCategory category, String receiptText) {
        this.merchantName = merchantName;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.category = category;
        this.receiptText = receiptText;
    }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public String getReceiptText() { return receiptText; }
    public void setReceiptText(String receiptText) { this.receiptText = receiptText; }
}
