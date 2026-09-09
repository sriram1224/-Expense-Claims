package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.entity.Claim;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import com.vsp.expenseclaims.entity.enums.UserRole;
import com.vsp.expenseclaims.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public void canEditClaim(User user, Claim claim) {
        if (user == null || claim == null) {
            throw new BusinessException("User or claim not found", HttpStatus.UNAUTHORIZED);
        }
        if (!claim.getEmployee().getId().equals(user.getId())) {
            throw new BusinessException("Only the owner can edit this claim", HttpStatus.FORBIDDEN);
        }
        if (claim.getStatus() != ClaimStatus.DRAFT && claim.getStatus() != ClaimStatus.REJECTED) {
            throw new BusinessException("Only draft or rejected claims can be edited", HttpStatus.BAD_REQUEST);
        }
    }

    public void canDeleteClaim(User user, Claim claim) {
        if (user == null || claim == null) {
            throw new BusinessException("User or claim not found", HttpStatus.UNAUTHORIZED);
        }
        if (!claim.getEmployee().getId().equals(user.getId())) {
            throw new BusinessException("Only the owner can delete this claim", HttpStatus.FORBIDDEN);
        }
        if (claim.getStatus() != ClaimStatus.DRAFT) {
            throw new BusinessException("Only draft claims can be deleted", HttpStatus.BAD_REQUEST);
        }
    }

    public void canSubmitClaim(User user, Claim claim) {
        if (user == null || claim == null) {
            throw new BusinessException("User or claim not found", HttpStatus.UNAUTHORIZED);
        }
        if (!claim.getEmployee().getId().equals(user.getId())) {
            throw new BusinessException("Only the owner can submit this claim", HttpStatus.FORBIDDEN);
        }
        if (claim.getStatus() != ClaimStatus.DRAFT && claim.getStatus() != ClaimStatus.REJECTED) {
            throw new BusinessException("Only draft or rejected claims can be submitted", HttpStatus.BAD_REQUEST);
        }
    }

    public void canApproveOrRejectClaim(User approver, Claim claim) {
        if (approver == null || claim == null) {
            throw new BusinessException("User or claim not found", HttpStatus.UNAUTHORIZED);
        }
        if (approver.getRole() != UserRole.MANAGER) {
            throw new BusinessException("Only managers can review and approve claims", HttpStatus.FORBIDDEN);
        }
        // Critical assignment rule: Manager must not sign off their own claim
        if (claim.getEmployee().getId().equals(approver.getId())) {
            throw new BusinessException("Manager cannot sign off their own claim. Self-approval is strictly prohibited.", HttpStatus.FORBIDDEN);
        }
        // Check if approver is the direct manager of the employee (or general manager check)
        if (claim.getEmployee().getManager() != null && !claim.getEmployee().getManager().getId().equals(approver.getId())) {
            throw new BusinessException("You are not the assigned manager for this employee (" + claim.getEmployee().getFullName() + ")", HttpStatus.FORBIDDEN);
        }
        if (claim.getStatus() != ClaimStatus.SUBMITTED) {
            throw new BusinessException("Only claims in SUBMITTED status can be approved or rejected", HttpStatus.BAD_REQUEST);
        }
    }

    public void canPayClaim(User financeUser, Claim claim) {
        if (financeUser == null || claim == null) {
            throw new BusinessException("User or claim not found", HttpStatus.UNAUTHORIZED);
        }
        if (financeUser.getRole() != UserRole.FINANCE) {
            throw new BusinessException("Only Finance personnel can disburse payouts", HttpStatus.FORBIDDEN);
        }
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BusinessException("Only claims in APPROVED status can be marked as paid", HttpStatus.BAD_REQUEST);
        }
    }

    public void canReviewDuplicates(User financeUser) {
        if (financeUser == null || financeUser.getRole() != UserRole.FINANCE) {
            throw new BusinessException("Only Finance personnel can review duplicate alerts", HttpStatus.FORBIDDEN);
        }
    }

    public void canViewFinanceReports(User user) {
        if (user == null || user.getRole() != UserRole.FINANCE) {
            throw new BusinessException("Only Finance personnel can view company-wide financial reports", HttpStatus.FORBIDDEN);
        }
    }
}
