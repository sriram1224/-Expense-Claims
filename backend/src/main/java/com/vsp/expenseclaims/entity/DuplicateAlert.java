package com.vsp.expenseclaims.entity;

import com.vsp.expenseclaims.entity.enums.DuplicateStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "duplicate_alerts")
public class DuplicateAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expense_item_id", nullable = false)
    private ExpenseItem expenseItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "matched_expense_item_id", nullable = false)
    private ExpenseItem matchedExpenseItem;

    @Column(name = "amount_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal amountScore;

    @Column(name = "merchant_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal merchantScore;

    @Column(name = "date_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal dateScore;

    @Column(name = "similarity_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal similarityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DuplicateStatus status = DuplicateStatus.PENDING_REVIEW;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    public DuplicateAlert() {
    }

    public DuplicateAlert(ExpenseItem expenseItem, ExpenseItem matchedExpenseItem, BigDecimal amountScore, BigDecimal merchantScore, BigDecimal dateScore, BigDecimal similarityScore) {
        this.expenseItem = expenseItem;
        this.matchedExpenseItem = matchedExpenseItem;
        this.amountScore = amountScore;
        this.merchantScore = merchantScore;
        this.dateScore = dateScore;
        this.similarityScore = similarityScore;
        this.status = DuplicateStatus.PENDING_REVIEW;
        this.detectedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.detectedAt == null) {
            this.detectedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = DuplicateStatus.PENDING_REVIEW;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpenseItem getExpenseItem() {
        return expenseItem;
    }

    public void setExpenseItem(ExpenseItem expenseItem) {
        this.expenseItem = expenseItem;
    }

    public ExpenseItem getMatchedExpenseItem() {
        return matchedExpenseItem;
    }

    public void setMatchedExpenseItem(ExpenseItem matchedExpenseItem) {
        this.matchedExpenseItem = matchedExpenseItem;
    }

    public BigDecimal getAmountScore() {
        return amountScore;
    }

    public void setAmountScore(BigDecimal amountScore) {
        this.amountScore = amountScore;
    }

    public BigDecimal getMerchantScore() {
        return merchantScore;
    }

    public void setMerchantScore(BigDecimal merchantScore) {
        this.merchantScore = merchantScore;
    }

    public BigDecimal getDateScore() {
        return dateScore;
    }

    public void setDateScore(BigDecimal dateScore) {
        this.dateScore = dateScore;
    }

    public BigDecimal getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(BigDecimal similarityScore) {
        this.similarityScore = similarityScore;
    }

    public DuplicateStatus getStatus() {
        return status;
    }

    public void setStatus(DuplicateStatus status) {
        this.status = status;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}
