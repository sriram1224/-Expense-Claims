package com.vsp.expenseclaims.dto;

import java.math.BigDecimal;

public class MonthlySpendResponse {

    private String month;
    private BigDecimal totalSpend;

    public MonthlySpendResponse() {}

    public MonthlySpendResponse(String month, BigDecimal totalSpend) {
        this.month = month;
        this.totalSpend = totalSpend != null ? totalSpend : BigDecimal.ZERO;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public BigDecimal getTotalSpend() { return totalSpend; }
    public void setTotalSpend(BigDecimal totalSpend) { this.totalSpend = totalSpend; }
}
