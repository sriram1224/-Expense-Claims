package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.config.UserContext;
import com.vsp.expenseclaims.dto.ClaimResponse;
import com.vsp.expenseclaims.dto.PaymentRequest;
import com.vsp.expenseclaims.dto.PaymentResponse;
import com.vsp.expenseclaims.entity.Claim;
import com.vsp.expenseclaims.entity.Payment;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import com.vsp.expenseclaims.exception.BusinessException;
import com.vsp.expenseclaims.repository.ClaimRepository;
import com.vsp.expenseclaims.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final ClaimRepository claimRepository;
    private final PaymentRepository paymentRepository;
    private final AuthorizationService authorizationService;
    private final WorkflowService workflowService;
    private final AtomicLong paymentCounter = new AtomicLong(2000);

    public PaymentService(ClaimRepository claimRepository,
                          PaymentRepository paymentRepository,
                          AuthorizationService authorizationService,
                          WorkflowService workflowService) {
        this.claimRepository = claimRepository;
        this.paymentRepository = paymentRepository;
        this.authorizationService = authorizationService;
        this.workflowService = workflowService;
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getApprovedClaims() {
        User financeUser = UserContext.getUser();
        authorizationService.canViewFinanceReports(financeUser);

        List<Claim> approved = claimRepository.findByStatusOrderBySubmittedAtDesc(ClaimStatus.APPROVED);
        return approved.stream()
                .map(ClaimResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse payClaim(Long claimId, PaymentRequest request) {
        User financeUser = UserContext.getUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));

        authorizationService.canPayClaim(financeUser, claim);
        workflowService.validateTransition(claim.getStatus(), ClaimStatus.PAID);

        String paymentRef = (request != null && request.getPaymentReference() != null && !request.getPaymentReference().trim().isEmpty())
                ? request.getPaymentReference()
                : generatePaymentReference();

        String remarks = (request != null && request.getRemarks() != null)
                ? request.getRemarks()
                : "Disbursement processed by Finance";

        Payment payment = new Payment(claim, financeUser, paymentRef, claim.getTotalAmount(), remarks);
        payment = paymentRepository.save(payment);

        claim.setStatus(ClaimStatus.PAID);
        claim.setPaidAt(LocalDateTime.now());
        claim.setPayment(payment);
        claimRepository.save(claim);

        return new PaymentResponse(payment);
    }

    public synchronized String generatePaymentReference() {
        int year = Year.now().getValue();
        return String.format("PAY-%d-%05d", year, paymentCounter.incrementAndGet());
    }
}
