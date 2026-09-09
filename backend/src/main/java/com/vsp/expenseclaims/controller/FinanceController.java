package com.vsp.expenseclaims.controller;

import com.vsp.expenseclaims.dto.ClaimResponse;
import com.vsp.expenseclaims.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance")
@Tag(name = "Finance", description = "Finance operations and approved payout queue")
public class FinanceController {

    private final PaymentService paymentService;

    public FinanceController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/approved-claims")
    @Operation(summary = "Get all approved claims ready for payout")
    public ResponseEntity<List<ClaimResponse>> getApprovedClaims() {
        return ResponseEntity.ok(paymentService.getApprovedClaims());
    }
}
