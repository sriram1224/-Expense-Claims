package com.vsp.expenseclaims.repository;

import com.vsp.expenseclaims.entity.DuplicateAlert;
import com.vsp.expenseclaims.entity.enums.DuplicateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DuplicateAlertRepository extends JpaRepository<DuplicateAlert, Long> {

    List<DuplicateAlert> findByStatusOrderByDetectedAtDesc(DuplicateStatus status);

    List<DuplicateAlert> findAllByOrderByDetectedAtDesc();

    long countByStatus(DuplicateStatus status);
}
