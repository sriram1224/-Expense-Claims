package com.vsp.expenseclaims.dto;

import java.math.BigDecimal;

public class OverLimitResponse {

    private Long employeeId;
    private String employee;
    private String email;
    private BigDecimal limit;
    private BigDecimal spent;
    private BigDecimal excess;

    public OverLimitResponse() {}

    public OverLimitResponse(Long employeeId, String employee, String email, BigDecimal limit, BigDecimal spent) {
        this.employeeId = employeeId;
        this.employee = employee;
        this.email = email;
        this.limit = limit != null ? limit : BigDecimal.ZERO;
        this.spent = spent != null ? spent : BigDecimal.ZERO;
        this.excess = this.spent.subtract(this.limit).max(BigDecimal.ZERO);
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployee() { return employee; }
    public void setEmployee(String employee) { this.employee = employee; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getLimit() { return limit; }
    public void setLimit(BigDecimal limit) { this.limit = limit; }

    public BigDecimal getSpent() { return spent; }
    public void setSpent(BigDecimal spent) { this.spent = spent; }

    public BigDecimal getExcess() { return excess; }
    public void setExcess(BigDecimal excess) { this.excess = excess; }
}
