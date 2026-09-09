package com.vsp.expenseclaims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vsp.expenseclaims.entity.enums.ExpenseCategory;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_items")
public class ExpenseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    @JsonIgnore
    private Claim claim;

    @Column(name = "merchant_name", nullable = false, length = 255)
    private String merchantName;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "receipt_text", columnDefinition = "TEXT")
    private String receiptText;

    @Column(name = "parsed_successfully")
    private Boolean parsedSuccessfully = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ExpenseItem() {
    }

    public ExpenseItem(Claim claim, String merchantName, LocalDate expenseDate, ExpenseCategory category, BigDecimal amount, String receiptText, Boolean parsedSuccessfully) {
        this.claim = claim;
        this.merchantName = merchantName;
        this.expenseDate = expenseDate;
        this.category = category;
        this.amount = amount;
        this.receiptText = receiptText;
        this.parsedSuccessfully = parsedSuccessfully != null ? parsedSuccessfully : false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReceiptText() {
        return receiptText;
    }

    public void setReceiptText(String receiptText) {
        this.receiptText = receiptText;
    }

    public Boolean getParsedSuccessfully() {
        return parsedSuccessfully;
    }

    public void setParsedSuccessfully(Boolean parsedSuccessfully) {
        this.parsedSuccessfully = parsedSuccessfully;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
