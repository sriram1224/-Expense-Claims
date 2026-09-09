package com.vsp.expenseclaims.dto;

import jakarta.validation.constraints.NotBlank;

public class RejectionRequest {

    @NotBlank(message = "Rejection reason is required")
    private String reason;

    public RejectionRequest() {}

    public RejectionRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
