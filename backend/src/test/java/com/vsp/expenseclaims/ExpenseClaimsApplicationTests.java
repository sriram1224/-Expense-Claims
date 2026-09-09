package com.vsp.expenseclaims;

import com.vsp.expenseclaims.config.UserContext;
import com.vsp.expenseclaims.dto.AddExpenseItemRequest;
import com.vsp.expenseclaims.dto.ApprovalRequest;
import com.vsp.expenseclaims.dto.CreateClaimRequest;
import com.vsp.expenseclaims.dto.ReceiptParseResponse;
import com.vsp.expenseclaims.entity.Claim;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.ClaimStatus;
import com.vsp.expenseclaims.entity.enums.ExpenseCategory;
import com.vsp.expenseclaims.entity.enums.UserRole;
import com.vsp.expenseclaims.exception.BusinessException;
import com.vsp.expenseclaims.repository.ClaimRepository;
import com.vsp.expenseclaims.repository.UserRepository;
import com.vsp.expenseclaims.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExpenseClaimsApplicationTests {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private ExpenseItemService expenseItemService;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ReceiptParsingService receiptParsingService;

    @Autowired
    private DuplicateDetectionService duplicateDetectionService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("Assignment Requirement: Manager must NOT sign off their own claim")
    void testManagerCannotApproveOwnClaim() {
        User rahul = userRepository.findByEmail("rahul@company.com").orElseThrow();
        UserContext.setContext(rahul.getId(), rahul.getRole(), rahul);

        // Find claim #6 which Rahul submitted
        Claim rahulClaim = claimRepository.findByEmployeeIdOrderByCreatedAtDesc(rahul.getId()).stream()
                .filter(c -> c.getStatus() == ClaimStatus.SUBMITTED)
                .findFirst()
                .orElseThrow();

        // Attempting to approve own claim must throw BusinessException
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            approvalService.approve(rahulClaim.getId(), new ApprovalRequest("Self approving"));
        });

        assertTrue(ex.getMessage().contains("cannot sign off their own claim") || ex.getMessage().contains("Self-approval is strictly prohibited"));
    }

    @Test
    @DisplayName("Assignment Requirement: A claim that has been paid is finished and should not go backwards")
    void testPaidClaimCannotGoBackwards() {
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            workflowService.validateTransition(ClaimStatus.PAID, ClaimStatus.APPROVED);
        });
        assertTrue(ex.getMessage().contains("cannot go backwards"));

        assertThrows(BusinessException.class, () -> {
            workflowService.validateTransition(ClaimStatus.PAID, ClaimStatus.DRAFT);
        });
    }

    @Test
    @DisplayName("Assignment Requirement: Smart receipt text parser extracts merchant, amount, date, and category")
    void testReceiptParsing() {
        String receipt = "UBER Airport Ride 450 INR 14 Aug 2026";
        ReceiptParseResponse parsed = receiptParsingService.parse(receipt);

        assertEquals("Uber", parsed.getMerchant());
        assertEquals(new BigDecimal("450"), parsed.getAmount());
        assertEquals(LocalDate.of(2026, 8, 14), parsed.getExpenseDate());
        assertEquals(ExpenseCategory.TAXI, parsed.getCategory());
        assertTrue(parsed.getConfidence() >= 80);
    }

    @Test
    @DisplayName("Assignment Requirement: Duplicate receipt detection flags matching receipts >= 80%")
    void testDuplicateDetection() {
        User ram = userRepository.findByEmail("ram@company.com").orElseThrow();
        UserContext.setContext(ram.getId(), ram.getRole(), ram);

        // Create a new draft claim
        var claimResp = claimService.createClaim(new CreateClaimRequest("Duplicate Test Claim"));

        // Add an item identical to the IndiGo flight already in database (IndiGo Airlines, ₹4800, 22 days ago)
        var itemResp = expenseItemService.addItem(claimResp.getClaimId(), new AddExpenseItemRequest(
                "IndiGo Airlines",
                new BigDecimal("4800.00"),
                LocalDate.now().minusDays(22),
                ExpenseCategory.TRAVEL,
                "IndiGo 6E 302 Mumbai to BLR ₹4,800"
        ));

        // It should trigger duplicate warning with high score
        assertTrue(itemResp.isDuplicateWarning());
        assertTrue(itemResp.getSimilarityScore().compareTo(new BigDecimal("80")) >= 0);
    }

    @Test
    @DisplayName("Lifecycle Requirement: Rejected claim can be edited and resubmitted")
    void testRejectedClaimCanBeEditedAndResubmitted() {
        User ram = userRepository.findByEmail("ram@company.com").orElseThrow();
        User rahul = userRepository.findByEmail("rahul@company.com").orElseThrow();

        // 1. Ram creates and submits a claim
        UserContext.setContext(ram.getId(), ram.getRole(), ram);
        var claim = claimService.createClaim(new CreateClaimRequest("Trip to Pune"));
        expenseItemService.addItem(claim.getClaimId(), new AddExpenseItemRequest("Taxi", new BigDecimal("350.00"), LocalDate.now(), ExpenseCategory.TAXI, "Auto ride 350"));
        var submitted = claimService.submitClaim(claim.getClaimId());
        assertEquals(ClaimStatus.SUBMITTED, submitted.getStatus());

        // 2. Manager Rahul rejects with reason
        UserContext.setContext(rahul.getId(), rahul.getRole(), rahul);
        var rejected = approvalService.reject(claim.getClaimId(), new com.vsp.expenseclaims.dto.RejectionRequest("Please fix purpose details"));
        assertEquals(ClaimStatus.REJECTED, rejected.getStatus());

        // 3. Ram edits rejected claim
        UserContext.setContext(ram.getId(), ram.getRole(), ram);
        var updated = claimService.updateClaim(claim.getClaimId(), new com.vsp.expenseclaims.dto.UpdateClaimRequest("Trip to Pune (Client Site Visit)"));
        assertEquals("Trip to Pune (Client Site Visit)", updated.getTitle());

        // 4. Ram resubmits the rejected claim
        var resubmitted = claimService.submitClaim(claim.getClaimId());
        assertEquals(ClaimStatus.SUBMITTED, resubmitted.getStatus());
    }

    @Test
    @DisplayName("Security & Integrity: Comprehensive Paid Claim Immutability (5 Prohibited Mutations)")
    void testComprehensivePaidClaimImmutability() {
        User ram = userRepository.findByEmail("ram@company.com").orElseThrow();
        User rahul = userRepository.findByEmail("rahul@company.com").orElseThrow();
        User anita = userRepository.findByEmail("anita@company.com").orElseThrow();

        // Claim #1 is pre-seeded as PAID
        Claim paidClaim = claimRepository.findById(1L).orElseThrow();
        assertEquals(ClaimStatus.PAID, paidClaim.getStatus());

        // Prohibited Mutation 1: Cannot edit title
        UserContext.setContext(ram.getId(), ram.getRole(), ram);
        assertThrows(BusinessException.class, () -> {
            claimService.updateClaim(1L, new com.vsp.expenseclaims.dto.UpdateClaimRequest("Hacked Title"));
        });

        // Prohibited Mutation 2: Cannot delete claim
        assertThrows(BusinessException.class, () -> {
            claimService.deleteClaim(1L);
        });

        // Prohibited Mutation 3: Cannot add expense items
        assertThrows(BusinessException.class, () -> {
            expenseItemService.addItem(1L, new AddExpenseItemRequest("Fake Item", new BigDecimal("100"), LocalDate.now(), ExpenseCategory.OTHER, "fake"));
        });

        // Prohibited Mutation 4: Manager cannot approve or reject a paid claim
        UserContext.setContext(rahul.getId(), rahul.getRole(), rahul);
        assertThrows(BusinessException.class, () -> {
            approvalService.approve(1L, new ApprovalRequest("Approve paid"));
        });
        assertThrows(BusinessException.class, () -> {
            approvalService.reject(1L, new com.vsp.expenseclaims.dto.RejectionRequest("Reject paid"));
        });

        // Prohibited Mutation 5: Finance cannot pay a paid claim again
        UserContext.setContext(anita.getId(), anita.getRole(), anita);
        assertThrows(BusinessException.class, () -> {
            paymentService.payClaim(1L, new com.vsp.expenseclaims.dto.PaymentRequest("PAY-AGAIN", "Double payout attempt"));
        });
    }

    @Test
    @DisplayName("Algorithm Verification: Deterministic Duplicate Detection Score Components (40-40-20)")
    void testDeterministicDuplicateScoreComponents() {
        // Evaluate an item matching Item #2 (Uber, 450 INR, Aug 19 2026)
        var alerts = duplicateDetectionService.getAllAlerts();
        assertFalse(alerts.isEmpty(), "Duplicate alerts must exist in seed data");

        var alert = alerts.stream()
                .filter(a -> a.getSimilarityScore().compareTo(new BigDecimal("90")) >= 0)
                .findFirst()
                .orElseThrow();

        // Verify score components
        assertNotNull(alert.getAmountScore());
        assertNotNull(alert.getMerchantScore());
        assertNotNull(alert.getDateScore());
        assertNotNull(alert.getSimilarityScore());

        // Verify sum
        BigDecimal calculatedTotal = alert.getAmountScore().add(alert.getMerchantScore()).add(alert.getDateScore());
        assertEquals(0, calculatedTotal.compareTo(alert.getSimilarityScore()), "Sum of components must equal similarity score");
        assertTrue(alert.getSimilarityScore().compareTo(new BigDecimal("80.00")) >= 0, "Flagged alert must have score >= 80");
    }

    @Test
    @DisplayName("Dashboard summary metrics returns valid counts and total spend")
    void testDashboardSummary() {
        var summary = dashboardService.getDashboardSummary();
        assertNotNull(summary);
        assertTrue(summary.getSubmittedClaims() > 0);
        assertTrue(summary.getApprovedClaims() > 0);
        assertTrue(summary.getPaidClaims() > 0);
        assertTrue(summary.getMonthlyTotalSpend().compareTo(BigDecimal.ZERO) > 0);
    }
}
