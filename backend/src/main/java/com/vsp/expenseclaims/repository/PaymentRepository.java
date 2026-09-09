package com.vsp.expenseclaims.repository;

import com.vsp.expenseclaims.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByClaimId(Long claimId);

    Optional<Payment> findByPaymentReference(String paymentReference);
}
