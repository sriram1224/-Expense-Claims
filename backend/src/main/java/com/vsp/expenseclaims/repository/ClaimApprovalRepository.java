package com.vsp.expenseclaims.repository;

import com.vsp.expenseclaims.entity.ClaimApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClaimApprovalRepository extends JpaRepository<ClaimApproval, Long> {

    List<ClaimApproval> findByClaimIdOrderByActionTimeDesc(Long claimId);
}
