package com.vsp.expenseclaims.controller;

import com.vsp.expenseclaims.config.UserContext;
import com.vsp.expenseclaims.dto.CategorySpendResponse;
import com.vsp.expenseclaims.dto.EmployeeSpendResponse;
import com.vsp.expenseclaims.dto.MonthlySpendResponse;
import com.vsp.expenseclaims.dto.OverLimitResponse;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.service.AuthorizationService;
import com.vsp.expenseclaims.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Finance reporting for monthly spend, categories, employees, and limit breaches")
public class ReportingController {

    private final ReportingService reportingService;
    private final AuthorizationService authorizationService;

    public ReportingController(ReportingService reportingService,
                               AuthorizationService authorizationService) {
        this.reportingService = reportingService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/monthly-spend")
    @Operation(summary = "Get current monthly spend total")
    public ResponseEntity<MonthlySpendResponse> getMonthlySpend() {
        User user = UserContext.getUser();
        authorizationService.canViewFinanceReports(user);
        return ResponseEntity.ok(reportingService.getMonthlySpend());
    }

    @GetMapping("/employee-spend")
    @Operation(summary = "Get spend broken down by employee")
    public ResponseEntity<List<EmployeeSpendResponse>> getEmployeeSpend() {
        User user = UserContext.getUser();
        authorizationService.canViewFinanceReports(user);
        return ResponseEntity.ok(reportingService.getEmployeeSpend());
    }

    @GetMapping("/category-spend")
    @Operation(summary = "Get spend broken down by expense category")
    public ResponseEntity<List<CategorySpendResponse>> getCategorySpend() {
        User user = UserContext.getUser();
        authorizationService.canViewFinanceReports(user);
        return ResponseEntity.ok(reportingService.getCategorySpend());
    }

    @GetMapping("/over-limit")
    @Operation(summary = "Get employees who have exceeded or are close to exceeding their monthly limits")
    public ResponseEntity<List<OverLimitResponse>> getOverLimit() {
        User user = UserContext.getUser();
        authorizationService.canViewFinanceReports(user);
        return ResponseEntity.ok(reportingService.getOverLimitUsers());
    }
}
