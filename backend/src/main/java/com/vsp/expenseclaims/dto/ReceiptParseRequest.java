package com.vsp.expenseclaims.dto;

import jakarta.validation.constraints.NotBlank;

public class ReceiptParseRequest {

    @NotBlank(message = "Receipt text is required")
    private String receiptText;

    public ReceiptParseRequest() {}

    public ReceiptParseRequest(String receiptText) {
        this.receiptText = receiptText;
    }

    public String getReceiptText() { return receiptText; }
    public void setReceiptText(String receiptText) { this.receiptText = receiptText; }
}
