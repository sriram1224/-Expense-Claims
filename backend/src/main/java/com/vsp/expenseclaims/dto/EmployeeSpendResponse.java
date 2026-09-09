package com.vsp.expenseclaims.dto;

import java.math.BigDecimal;

public class EmployeeSpendResponse {

    private Long employeeId;
    private String employee;
    private String email;
    private BigDecimal total;

    public EmployeeSpendResponse() {}

    public EmployeeSpendResponse(Long employeeId, String employee, String email, BigDecimal total) {
        this.employeeId = employeeId;
        this.employee = employee;
        this.email = email;
        this.total = total != null ? total : BigDecimal.ZERO;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployee() { return employee; }
    public void setEmployee(String employee) { this.employee = employee; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
