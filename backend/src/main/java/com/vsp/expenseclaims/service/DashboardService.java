package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.dto.DashboardSummaryResponse;
import com.vsp.expenseclaims.dto.MonthlySpendResponse;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import com.vsp.expenseclaims.entity.enums.DuplicateStatus;
import com.vsp.expenseclaims.repository.ClaimRepository;
import com.vsp.expenseclaims.repository.DuplicateAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final ClaimRepository claimRepository;
    private final DuplicateAlertRepository duplicateAlertRepository;
    private final ReportingService reportingService;

    public DashboardService(ClaimRepository claimRepository,
                            DuplicateAlertRepository duplicateAlertRepository,
                            ReportingService reportingService) {
        this.claimRepository = claimRepository;
        this.duplicateAlertRepository = duplicateAlertRepository;
        this.reportingService = reportingService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        long drafts = claimRepository.countByStatus(ClaimStatus.DRAFT);
        long submitted = claimRepository.countByStatus(ClaimStatus.SUBMITTED);
        long approved = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long paid = claimRepository.countByStatus(ClaimStatus.PAID);
        long dups = duplicateAlertRepository.countByStatus(DuplicateStatus.PENDING_REVIEW);
        long overLimit = reportingService.getOverLimitUsers().size();

        MonthlySpendResponse monthlySpend = reportingService.getMonthlySpend();

        return new DashboardSummaryResponse(
                drafts,
                submitted,
                approved,
                paid,
                dups,
                overLimit,
                monthlySpend.getTotalSpend()
        );
    }
}
