package com.vsp.expenseclaims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vsp.expenseclaims.entity.enums.ApprovalAction;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_approvals")
public class ClaimApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    @JsonIgnore
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalAction action;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    public ClaimApproval() {
    }

    public ClaimApproval(Claim claim, User approver, ApprovalAction action, String comments) {
        this.claim = claim;
        this.approver = approver;
        this.action = action;
        this.comments = comments;
        this.actionTime = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.actionTime == null) {
            this.actionTime = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public ApprovalAction getAction() {
        return action;
    }

    public void setAction(ApprovalAction action) {
        this.action = action;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalDateTime actionTime) {
        this.actionTime = actionTime;
    }
}
