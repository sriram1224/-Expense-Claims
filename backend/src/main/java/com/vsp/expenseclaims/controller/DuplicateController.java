package com.vsp.expenseclaims.controller;

import com.vsp.expenseclaims.config.UserContext;
import com.vsp.expenseclaims.dto.DuplicateAlertResponse;
import com.vsp.expenseclaims.dto.ReviewDuplicateRequest;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.service.AuthorizationService;
import com.vsp.expenseclaims.service.DuplicateDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/duplicates")
@Tag(name = "Duplicates", description = "Duplicate receipt alerts and resolution")
public class DuplicateController {

    private final DuplicateDetectionService duplicateDetectionService;
    private final AuthorizationService authorizationService;

    public DuplicateController(DuplicateDetectionService duplicateDetectionService,
                               AuthorizationService authorizationService) {
        this.duplicateDetectionService = duplicateDetectionService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @Operation(summary = "Get all duplicate receipt alerts with component score breakdowns")
    public ResponseEntity<List<DuplicateAlertResponse>> getAlerts() {
        User user = UserContext.getUser();
        authorizationService.canReviewDuplicates(user);
        return ResponseEntity.ok(duplicateDetectionService.getAllAlerts());
    }

    @PostMapping("/{duplicateId}/review")
    @Operation(summary = "Finance reviews a duplicate alert (CONFIRMED_DUPLICATE or FALSE_POSITIVE)")
    public ResponseEntity<DuplicateAlertResponse> reviewAlert(@PathVariable Long duplicateId,
                                                              @Valid @RequestBody ReviewDuplicateRequest request) {
        User user = UserContext.getUser();
        authorizationService.canReviewDuplicates(user);
        return ResponseEntity.ok(duplicateDetectionService.reviewAlert(duplicateId, request.getDecision()));
    }
}
