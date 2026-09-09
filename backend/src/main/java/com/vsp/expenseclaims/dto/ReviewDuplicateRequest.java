package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.enums.DuplicateStatus;
import jakarta.validation.constraints.NotNull;

public class ReviewDuplicateRequest {

    @NotNull(message = "Decision is required")
    private DuplicateStatus decision;

    public ReviewDuplicateRequest() {}

    public ReviewDuplicateRequest(DuplicateStatus decision) {
        this.decision = decision;
    }

    public DuplicateStatus getDecision() { return decision; }
    public void setDecision(DuplicateStatus decision) { this.decision = decision; }
}
