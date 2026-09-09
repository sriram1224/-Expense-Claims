package com.vsp.expenseclaims.repository;

import com.vsp.expenseclaims.entity.Claim;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<Claim> findByEmployeeIdAndStatusOrderByCreatedAtDesc(Long employeeId, ClaimStatus status);

    List<Claim> findByStatusOrderBySubmittedAtDesc(ClaimStatus status);

    List<Claim> findAllByOrderByCreatedAtDesc();

    @Query("SELECT c FROM Claim c WHERE c.employee.manager.id = :managerId AND c.status = :status ORDER BY c.submittedAt ASC")
    List<Claim> findPendingApprovalsForManager(@Param("managerId") Long managerId, @Param("status") ClaimStatus status);

    @Query("SELECT c FROM Claim c WHERE c.employee.manager.id = :managerId ORDER BY c.createdAt DESC")
    List<Claim> findTeamClaimsForManager(@Param("managerId") Long managerId);

    long countByStatus(ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.totalAmount), 0) FROM Claim c WHERE c.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.totalAmount), 0) FROM Claim c WHERE c.status IN ('SUBMITTED', 'APPROVED', 'PAID') AND c.employee.id = :employeeId")
    BigDecimal sumSpentByEmployee(@Param("employeeId") Long employeeId);
}
