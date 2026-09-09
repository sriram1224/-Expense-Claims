package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long claimId;
    private String paymentReference;
    private BigDecimal amount;
    private Long processedById;
    private String processedByName;
    private LocalDateTime paymentDate;
    private String remarks;

    public PaymentResponse() {}

    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.claimId = payment.getClaim() != null ? payment.getClaim().getId() : null;
        this.paymentReference = payment.getPaymentReference();
        this.amount = payment.getAmount();
        if (payment.getProcessedBy() != null) {
            this.processedById = payment.getProcessedBy().getId();
            this.processedByName = payment.getProcessedBy().getFullName();
        }
        this.paymentDate = payment.getPaymentDate();
        this.remarks = payment.getRemarks();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getProcessedById() { return processedById; }
    public void setProcessedById(Long processedById) { this.processedById = processedById; }

    public String getProcessedByName() { return processedByName; }
    public void setProcessedByName(String processedByName) { this.processedByName = processedByName; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
