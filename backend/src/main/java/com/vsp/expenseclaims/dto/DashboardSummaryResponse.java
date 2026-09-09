package com.vsp.expenseclaims.dto;

import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private long draftClaims;
    private long submittedClaims;
    private long approvedClaims;
    private long paidClaims;
    private long duplicateAlerts;
    private long overLimitCount;
    private BigDecimal monthlyTotalSpend;

    public DashboardSummaryResponse() {}

    public DashboardSummaryResponse(long draftClaims, long submittedClaims, long approvedClaims, long paidClaims, long duplicateAlerts, long overLimitCount, BigDecimal monthlyTotalSpend) {
        this.draftClaims = draftClaims;
        this.submittedClaims = submittedClaims;
        this.approvedClaims = approvedClaims;
        this.paidClaims = paidClaims;
        this.duplicateAlerts = duplicateAlerts;
        this.overLimitCount = overLimitCount;
        this.monthlyTotalSpend = monthlyTotalSpend != null ? monthlyTotalSpend : BigDecimal.ZERO;
    }

    public long getDraftClaims() { return draftClaims; }
    public void setDraftClaims(long draftClaims) { this.draftClaims = draftClaims; }

    public long getSubmittedClaims() { return submittedClaims; }
    public void setSubmittedClaims(long submittedClaims) { this.submittedClaims = submittedClaims; }

    public long getApprovedClaims() { return approvedClaims; }
    public void setApprovedClaims(long approvedClaims) { this.approvedClaims = approvedClaims; }

    public long getPaidClaims() { return paidClaims; }
    public void setPaidClaims(long paidClaims) { this.paidClaims = paidClaims; }

    public long getDuplicateAlerts() { return duplicateAlerts; }
    public void setDuplicateAlerts(long duplicateAlerts) { this.duplicateAlerts = duplicateAlerts; }

    public long getOverLimitCount() { return overLimitCount; }
    public void setOverLimitCount(long overLimitCount) { this.overLimitCount = overLimitCount; }

    public BigDecimal getMonthlyTotalSpend() { return monthlyTotalSpend; }
    public void setMonthlyTotalSpend(BigDecimal monthlyTotalSpend) { this.monthlyTotalSpend = monthlyTotalSpend; }
}
