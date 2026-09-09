package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import com.vsp.expenseclaims.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    public void validateTransition(ClaimStatus current, ClaimStatus next) {
        if (current == ClaimStatus.PAID) {
            throw new BusinessException("A claim that has been paid is finished and cannot go backwards.");
        }

        boolean valid = switch (current) {
            case DRAFT -> (next == ClaimStatus.SUBMITTED);
            case SUBMITTED -> (next == ClaimStatus.APPROVED || next == ClaimStatus.REJECTED);
            case REJECTED -> (next == ClaimStatus.SUBMITTED);
            case APPROVED -> (next == ClaimStatus.PAID);
            default -> false;
        };

        if (!valid) {
            throw new BusinessException("Invalid claim status transition from " + current + " to " + next);
        }
    }
}
