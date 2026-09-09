package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.config.UserContext;
import com.vsp.expenseclaims.dto.ClaimDetailResponse;
import com.vsp.expenseclaims.dto.ClaimResponse;
import com.vsp.expenseclaims.dto.CreateClaimRequest;
import com.vsp.expenseclaims.dto.UpdateClaimRequest;
import com.vsp.expenseclaims.entity.Claim;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import com.vsp.expenseclaims.exception.BusinessException;
import com.vsp.expenseclaims.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final WorkflowService workflowService;
    private final AuthorizationService authorizationService;
    private final AtomicLong claimCounter = new AtomicLong(1000);

    public ClaimService(ClaimRepository claimRepository,
                        WorkflowService workflowService,
                        AuthorizationService authorizationService) {
        this.claimRepository = claimRepository;
        this.workflowService = workflowService;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public ClaimResponse createClaim(CreateClaimRequest request) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException("Current authenticated user context is missing");
        }

        String claimNumber = generateClaimNumber();
        Claim claim = new Claim(claimNumber, request.getTitle(), currentUser);
        claim = claimRepository.save(claim);

        return new ClaimResponse(claim);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getMyClaims(ClaimStatus status) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException("Current authenticated user context is missing");
        }

        List<Claim> claims;
        if (status != null) {
            claims = claimRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), status);
        } else {
            claims = claimRepository.findByEmployeeIdOrderByCreatedAtDesc(currentUser.getId());
        }

        return claims.stream()
                .map(ClaimResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClaimDetailResponse getClaim(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));
        return new ClaimDetailResponse(claim);
    }

    @Transactional
    public ClaimResponse updateClaim(Long claimId, UpdateClaimRequest request) {
        User currentUser = UserContext.getUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));

        authorizationService.canEditClaim(currentUser, claim);

        claim.setTitle(request.getTitle());
        claim = claimRepository.save(claim);

        return new ClaimResponse(claim);
    }

    @Transactional
    public void deleteClaim(Long claimId) {
        User currentUser = UserContext.getUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));

        authorizationService.canDeleteClaim(currentUser, claim);
        claimRepository.delete(claim);
    }

    @Transactional
    public ClaimResponse submitClaim(Long claimId) {
        User currentUser = UserContext.getUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));

        authorizationService.canSubmitClaim(currentUser, claim);

        if (claim.getExpenseItems() == null || claim.getExpenseItems().isEmpty()) {
            throw new BusinessException("Cannot submit a claim with zero expense items. Please add at least one expense.");
        }

        workflowService.validateTransition(claim.getStatus(), ClaimStatus.SUBMITTED);

        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setSubmittedAt(LocalDateTime.now());
        claim = claimRepository.save(claim);

        return new ClaimResponse(claim);
    }

    public synchronized String generateClaimNumber() {
        int year = Year.now().getValue();
        long count = claimRepository.count() + claimCounter.incrementAndGet();
        return String.format("CLM-%d-%05d", year, count);
    }
}
