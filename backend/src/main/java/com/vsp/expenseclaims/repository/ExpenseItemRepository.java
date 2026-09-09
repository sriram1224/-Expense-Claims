package com.vsp.expenseclaims.repository;

import com.vsp.expenseclaims.entity.ExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {

    List<ExpenseItem> findByClaimId(Long claimId);

    @Query("SELECT e FROM ExpenseItem e WHERE e.id != :currentItemId AND e.claim.status != 'REJECTED' AND e.claim.status != 'DRAFT'")
    List<ExpenseItem> findCandidateItemsForDuplicateCheck(@Param("currentItemId") Long currentItemId);

    @Query("SELECT e.category as category, SUM(e.amount) as total FROM ExpenseItem e WHERE e.claim.status IN ('APPROVED', 'PAID') GROUP BY e.category")
    List<Object[]> getCategorySpendSummary();

    @Query("SELECT e.claim.employee.id as employeeId, e.claim.employee.firstName as firstName, e.claim.employee.lastName as lastName, e.claim.employee.email as email, e.claim.employee.monthlyLimit as monthlyLimit, SUM(e.amount) as totalSpent FROM ExpenseItem e WHERE e.claim.status IN ('SUBMITTED', 'APPROVED', 'PAID') GROUP BY e.claim.employee.id, e.claim.employee.firstName, e.claim.employee.lastName, e.claim.employee.email, e.claim.employee.monthlyLimit")
    List<Object[]> getEmployeeSpendSummary();
}
