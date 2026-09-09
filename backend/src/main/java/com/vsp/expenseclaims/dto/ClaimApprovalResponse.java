package com.vsp.expenseclaims.dto;

import com.vsp.expenseclaims.entity.ClaimApproval;
import com.vsp.expenseclaims.entity.enums.ApprovalAction;
import java.time.LocalDateTime;

public class ClaimApprovalResponse {

    private Long id;
    private Long approverId;
    private String approverName;
    private ApprovalAction action;
    private String comments;
    private LocalDateTime actionTime;

    public ClaimApprovalResponse() {}

    public ClaimApprovalResponse(ClaimApproval approval) {
        this.id = approval.getId();
        if (approval.getApprover() != null) {
            this.approverId = approval.getApprover().getId();
            this.approverName = approval.getApprover().getFullName();
        }
        this.action = approval.getAction();
        this.comments = approval.getComments();
        this.actionTime = approval.getActionTime();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public ApprovalAction getAction() { return action; }
    public void setAction(ApprovalAction action) { this.action = action; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
}
