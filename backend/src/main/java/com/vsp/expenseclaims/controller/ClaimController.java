package com.vsp.expenseclaims.controller;

import com.vsp.expenseclaims.dto.*;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import com.vsp.expenseclaims.service.ApprovalService;
import com.vsp.expenseclaims.service.ClaimService;
import com.vsp.expenseclaims.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Claims", description = "Claim lifecycle, actions, and workflow management")
public class ClaimController {

    private final ClaimService claimService;
    private final ApprovalService approvalService;
    private final PaymentService paymentService;

    public ClaimController(ClaimService claimService,
                           ApprovalService approvalService,
                           PaymentService paymentService) {
        this.claimService = claimService;
        this.approvalService = approvalService;
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Create a new draft claim")
    public ResponseEntity<ClaimResponse> createClaim(@Valid @RequestBody CreateClaimRequest request) {
        ClaimResponse response = claimService.createClaim(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @Operation(summary = "Get claims filed by the currently authenticated user")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(@RequestParam(required = false) ClaimStatus status) {
        return ResponseEntity.ok(claimService.getMyClaims(status));
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get claims awaiting approval for the current manager")
    public ResponseEntity<List<ClaimResponse>> getPendingApprovals() {
        return ResponseEntity.ok(approvalService.getPendingApprovals());
    }

    @GetMapping("/team")
    @Operation(summary = "Get all claims submitted by team members of the current manager")
    public ResponseEntity<List<ClaimResponse>> getTeamClaims() {
        return ResponseEntity.ok(approvalService.getTeamClaims());
    }

    @GetMapping("/{claimId}")
    @Operation(summary = "Get full details of a specific claim")
    public ResponseEntity<ClaimDetailResponse> getClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.getClaim(claimId));
    }

    @PutMapping("/{claimId}")
    @Operation(summary = "Update claim title (Draft or Rejected only)")
    public ResponseEntity<ClaimResponse> updateClaim(@PathVariable Long claimId, @Valid @RequestBody UpdateClaimRequest request) {
        return ResponseEntity.ok(claimService.updateClaim(claimId, request));
    }

    @DeleteMapping("/{claimId}")
    @Operation(summary = "Delete a draft claim")
    public ResponseEntity<Void> deleteClaim(@PathVariable Long claimId) {
        claimService.deleteClaim(claimId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{claimId}/submit")
    @Operation(summary = "Submit a draft/rejected claim for manager approval")
    public ResponseEntity<ClaimResponse> submitClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.submitClaim(claimId));
    }

    @PostMapping("/{claimId}/approve")
    @Operation(summary = "Manager approves a submitted claim")
    public ResponseEntity<ClaimResponse> approveClaim(@PathVariable Long claimId, @RequestBody(required = false) ApprovalRequest request) {
        return ResponseEntity.ok(approvalService.approve(claimId, request));
    }

    @PostMapping("/{claimId}/reject")
    @Operation(summary = "Manager rejects a submitted claim with reason")
    public ResponseEntity<ClaimResponse> rejectClaim(@PathVariable Long claimId, @Valid @RequestBody RejectionRequest request) {
        return ResponseEntity.ok(approvalService.reject(claimId, request));
    }

    @PostMapping("/{claimId}/pay")
    @Operation(summary = "Finance disburses payment for an approved claim (terminal state)")
    public ResponseEntity<PaymentResponse> payClaim(@PathVariable Long claimId, @RequestBody(required = false) PaymentRequest request) {
        return ResponseEntity.ok(paymentService.payClaim(claimId, request));
    }
}
