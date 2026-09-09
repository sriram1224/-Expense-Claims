package com.vsp.expenseclaims.dto;

public class PaymentRequest {

    private String paymentReference;
    private String remarks;

    public PaymentRequest() {}

    public PaymentRequest(String paymentReference, String remarks) {
        this.paymentReference = paymentReference;
        this.remarks = remarks;
    }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
