package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.enums.ExpenseCategory;
import java.math.BigDecimal;

public class CategorySpendResponse {

    private ExpenseCategory category;
    private BigDecimal total;

    public CategorySpendResponse() {}

    public CategorySpendResponse(ExpenseCategory category, BigDecimal total) {
        this.category = category;
        this.total = total != null ? total : BigDecimal.ZERO;
    }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
