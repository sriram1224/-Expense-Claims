package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.config.UserContext;
import com.vsp.expenseclaims.dto.ApprovalRequest;
import com.vsp.expenseclaims.dto.ClaimResponse;
import com.vsp.expenseclaims.dto.RejectionRequest;
import com.vsp.expenseclaims.entity.Claim;
import com.vsp.expenseclaims.entity.ClaimApproval;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.ApprovalAction;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import com.vsp.expenseclaims.exception.BusinessException;
import com.vsp.expenseclaims.repository.ClaimApprovalRepository;
import com.vsp.expenseclaims.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    private final ClaimRepository claimRepository;
    private final ClaimApprovalRepository approvalRepository;
    private final AuthorizationService authorizationService;
    private final WorkflowService workflowService;

    public ApprovalService(ClaimRepository claimRepository,
                           ClaimApprovalRepository approvalRepository,
                           AuthorizationService authorizationService,
                           WorkflowService workflowService) {
        this.claimRepository = claimRepository;
        this.approvalRepository = approvalRepository;
        this.authorizationService = authorizationService;
        this.workflowService = workflowService;
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getPendingApprovals() {
        User currentManager = UserContext.getUser();
        if (currentManager == null) {
            throw new BusinessException("Authenticated manager context is missing");
        }

        // Return claims submitted by this manager's team members
        List<Claim> pending = claimRepository.findPendingApprovalsForManager(currentManager.getId(), ClaimStatus.SUBMITTED);
        return pending.stream()
                .map(ClaimResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getTeamClaims() {
        User currentManager = UserContext.getUser();
        if (currentManager == null) {
            throw new BusinessException("Authenticated manager context is missing");
        }

        List<Claim> teamClaims = claimRepository.findTeamClaimsForManager(currentManager.getId());
        return teamClaims.stream()
                .map(ClaimResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClaimResponse approve(Long claimId, ApprovalRequest request) {
        User currentManager = UserContext.getUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));

        authorizationService.canApproveOrRejectClaim(currentManager, claim);
        workflowService.validateTransition(claim.getStatus(), ClaimStatus.APPROVED);

        ClaimApproval approval = new ClaimApproval(
                claim,
                currentManager,
                ApprovalAction.APPROVED,
                request != null && request.getComments() != null ? request.getComments() : "Approved by manager"
        );
        approvalRepository.save(approval);

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAt(LocalDateTime.now());
        claim.getApprovals().add(approval);
        claim = claimRepository.save(claim);

        return new ClaimResponse(claim);
    }

    @Transactional
    public ClaimResponse reject(Long claimId, RejectionRequest request) {
        User currentManager = UserContext.getUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException("Claim not found with ID: " + claimId));

        authorizationService.canApproveOrRejectClaim(currentManager, claim);
        workflowService.validateTransition(claim.getStatus(), ClaimStatus.REJECTED);

        ClaimApproval rejection = new ClaimApproval(
                claim,
                currentManager,
                ApprovalAction.REJECTED,
                request.getReason()
        );
        approvalRepository.save(rejection);

        claim.setStatus(ClaimStatus.REJECTED);
        claim.getApprovals().add(rejection);
        claim = claimRepository.save(claim);

        return new ClaimResponse(claim);
    }
}
