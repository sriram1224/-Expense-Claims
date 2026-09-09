package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.dto.CategorySpendResponse;
import com.vsp.expenseclaims.dto.EmployeeSpendResponse;
import com.vsp.expenseclaims.dto.MonthlySpendResponse;
import com.vsp.expenseclaims.dto.OverLimitResponse;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import com.vsp.expenseclaims.entity.enums.ExpenseCategory;
import com.vsp.expenseclaims.repository.ClaimRepository;
import com.vsp.expenseclaims.repository.ExpenseItemRepository;
import com.vsp.expenseclaims.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportingService {

    private final ClaimRepository claimRepository;
    private final ExpenseItemRepository expenseItemRepository;
    private final UserRepository userRepository;

    public ReportingService(ClaimRepository claimRepository,
                            ExpenseItemRepository expenseItemRepository,
                            UserRepository userRepository) {
        this.claimRepository = claimRepository;
        this.expenseItemRepository = expenseItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MonthlySpendResponse getMonthlySpend() {
        BigDecimal paidSum = claimRepository.sumTotalAmountByStatus(ClaimStatus.PAID);
        BigDecimal approvedSum = claimRepository.sumTotalAmountByStatus(ClaimStatus.APPROVED);
        BigDecimal total = (paidSum != null ? paidSum : BigDecimal.ZERO)
                .add(approvedSum != null ? approvedSum : BigDecimal.ZERO);
        return new MonthlySpendResponse(YearMonth.now().toString(), total);
    }

    @Transactional(readOnly = true)
    public List<EmployeeSpendResponse> getEmployeeSpend() {
        List<Object[]> rows = expenseItemRepository.getEmployeeSpendSummary();
        List<EmployeeSpendResponse> list = new ArrayList<>();
        for (Object[] r : rows) {
            Long empId = ((Number) r[0]).longValue();
            String name = r[1] + " " + r[2];
            String email = (String) r[3];
            BigDecimal total = (BigDecimal) r[5];
            list.add(new EmployeeSpendResponse(empId, name, email, total));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<CategorySpendResponse> getCategorySpend() {
        List<Object[]> rows = expenseItemRepository.getCategorySpendSummary();
        List<CategorySpendResponse> list = new ArrayList<>();
        for (Object[] r : rows) {
            ExpenseCategory cat = (ExpenseCategory) r[0];
            BigDecimal total = (BigDecimal) r[1];
            list.add(new CategorySpendResponse(cat, total));
        }
        // Fill in missing categories with zero if any
        for (ExpenseCategory ec : ExpenseCategory.values()) {
            if (list.stream().noneMatch(c -> c.getCategory() == ec)) {
                list.add(new CategorySpendResponse(ec, BigDecimal.ZERO));
            }
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<OverLimitResponse> getOverLimitUsers() {
        List<User> users = userRepository.findByActiveTrue();
        List<OverLimitResponse> overLimitList = new ArrayList<>();

        for (User u : users) {
            if (u.getMonthlyLimit() != null && u.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal spent = claimRepository.sumSpentByEmployee(u.getId());
                if (spent != null && spent.compareTo(u.getMonthlyLimit()) >= 0) {
                    overLimitList.add(new OverLimitResponse(u.getId(), u.getFullName(), u.getEmail(), u.getMonthlyLimit(), spent));
                }
            }
        }
        return overLimitList;
    }
}
