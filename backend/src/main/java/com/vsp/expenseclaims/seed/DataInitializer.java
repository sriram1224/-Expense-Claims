package com.vsp.expenseclaims.seed;

import com.vsp.expenseclaims.entity.*;
import com.vsp.expenseclaims.entity.enums.*;
import com.vsp.expenseclaims.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClaimRepository claimRepository;
    private final ExpenseItemRepository expenseItemRepository;
    private final ClaimApprovalRepository approvalRepository;
    private final PaymentRepository paymentRepository;
    private final DuplicateAlertRepository duplicateAlertRepository;

    public DataInitializer(UserRepository userRepository,
                           ClaimRepository claimRepository,
                           ExpenseItemRepository expenseItemRepository,
                           ClaimApprovalRepository approvalRepository,
                           PaymentRepository paymentRepository,
                           DuplicateAlertRepository duplicateAlertRepository) {
        this.userRepository = userRepository;
        this.claimRepository = claimRepository;
        this.expenseItemRepository = expenseItemRepository;
        this.approvalRepository = approvalRepository;
        this.paymentRepository = paymentRepository;
        this.duplicateAlertRepository = duplicateAlertRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already initialized
        }

        // ==========================================
        // 1. Seed Users (Managers, Finance, Staff)
        // ==========================================
        User rahul = new User("MGR-001", "Rahul", "Sharma", "rahul@company.com", UserRole.MANAGER, new BigDecimal("50000.00"), null);
        User vikram = new User("MGR-002", "Vikram", "Patel", "vikram@company.com", UserRole.MANAGER, new BigDecimal("60000.00"), null);
        User anita = new User("FIN-001", "Anita", "Desai", "anita@company.com", UserRole.FINANCE, new BigDecimal("100000.00"), null);

        rahul = userRepository.save(rahul);
        vikram = userRepository.save(vikram);
        anita = userRepository.save(anita);

        // Staff for Rahul's team
        User ram = userRepository.save(new User("EMP-101", "Ram", "Kumar", "ram@company.com", UserRole.STAFF, new BigDecimal("15000.00"), rahul));
        User priya = userRepository.save(new User("EMP-102", "Priya", "Nair", "priya@company.com", UserRole.STAFF, new BigDecimal("12000.00"), rahul));
        User amit = userRepository.save(new User("EMP-103", "Amit", "Verma", "amit@company.com", UserRole.STAFF, new BigDecimal("20000.00"), rahul));
        User sneha = userRepository.save(new User("EMP-104", "Sneha", "Kulkarni", "sneha@company.com", UserRole.STAFF, new BigDecimal("10000.00"), rahul));
        User rohan = userRepository.save(new User("EMP-105", "Rohan", "Gupta", "rohan@company.com", UserRole.STAFF, new BigDecimal("18000.00"), rahul));

        // Staff for Vikram's team
        User karthik = userRepository.save(new User("EMP-106", "Karthik", "Raja", "karthik@company.com", UserRole.STAFF, new BigDecimal("15000.00"), vikram));
        User divya = userRepository.save(new User("EMP-107", "Divya", "Menon", "divya@company.com", UserRole.STAFF, new BigDecimal("12000.00"), vikram));
        User suresh = userRepository.save(new User("EMP-108", "Suresh", "Reddy", "suresh@company.com", UserRole.STAFF, new BigDecimal("25000.00"), vikram));
        User ananya = userRepository.save(new User("EMP-109", "Ananya", "Das", "ananya@company.com", UserRole.STAFF, new BigDecimal("14000.00"), vikram));
        User neha = userRepository.save(new User("EMP-110", "Neha", "Joshi", "neha@company.com", UserRole.STAFF, new BigDecimal("16000.00"), vikram));

        // ==========================================
        // 2. Seed Claims & Expense Items
        // ==========================================

        // --- Claim 1: Ram Kumar - Bangalore Client Visit (PAID) ---
        Claim c1 = createClaimHelper("CLM-2026-00001", "Bangalore Client Visit", ram, ClaimStatus.PAID,
                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(18), LocalDateTime.now().minusDays(15));
        ExpenseItem e1_1 = addExpenseHelper(c1, "IndiGo Airlines", LocalDate.now().minusDays(22), ExpenseCategory.TRAVEL, new BigDecimal("4800.00"), "IndiGo 6E 302 Mumbai to BLR ₹4,800 on 18 Aug");
        ExpenseItem e1_2 = addExpenseHelper(c1, "Uber", LocalDate.now().minusDays(21), ExpenseCategory.TAXI, new BigDecimal("450.00"), "UBER Airport Ride 450 INR 19 Aug 2026");
        ExpenseItem e1_3 = addExpenseHelper(c1, "Swiggy", LocalDate.now().minusDays(21), ExpenseCategory.MEALS, new BigDecimal("850.00"), "Swiggy Order #8921 Dinner ₹850");
        saveClaimAndItems(c1, List.of(e1_1, e1_2, e1_3));
        approvalRepository.save(new ClaimApproval(c1, rahul, ApprovalAction.APPROVED, "Verified travel tickets. Approved."));
        paymentRepository.save(new Payment(c1, anita, "PAY-2026-00001", c1.getTotalAmount(), "Bank transfer completed via NEFT"));

        // --- Claim 2: Ram Kumar - Tech Summit & Hardware (SUBMITTED - Over Limit showcase) ---
        Claim c2 = createClaimHelper("CLM-2026-00002", "Tech Summit & Peripherals", ram, ClaimStatus.SUBMITTED,
                LocalDateTime.now().minusDays(2), null, null);
        ExpenseItem e2_1 = addExpenseHelper(c2, "Croma Electronics", LocalDate.now().minusDays(3), ExpenseCategory.SUPPLIES, new BigDecimal("8500.00"), "Croma Store Bangalore - Monitor and Cable Rs. 8500");
        ExpenseItem e2_2 = addExpenseHelper(c2, "Starbucks", LocalDate.now().minusDays(2), ExpenseCategory.MEALS, new BigDecimal("650.00"), "Starbucks Coffee & Snacks INR 650.00 07-Sep-2026");
        ExpenseItem e2_3 = addExpenseHelper(c2, "City Taxi / Auto", LocalDate.now().minusDays(2), ExpenseCategory.TAXI, new BigDecimal("2600.00"), "City Taxi Summit Travel 2600 INR");
        saveClaimAndItems(c2, List.of(e2_1, e2_2, e2_3));
        // Note: Ram's total spend is 6100 + 11750 = 17850 which exceeds his limit of 15000!

        // --- Claim 3: Priya Nair - Client Presentation Travel (APPROVED - pending payout) ---
        Claim c3 = createClaimHelper("CLM-2026-00003", "Client Presentation Travel", priya, ClaimStatus.APPROVED,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(2), null);
        ExpenseItem e3_1 = addExpenseHelper(c3, "Air India", LocalDate.now().minusDays(6), ExpenseCategory.TRAVEL, new BigDecimal("5200.00"), "Air India AI-441 Flight 5200 INR");
        ExpenseItem e3_2 = addExpenseHelper(c3, "Uber", LocalDate.now().minusDays(5), ExpenseCategory.TAXI, new BigDecimal("580.00"), "Uber Premier Ride to client office ₹580");
        saveClaimAndItems(c3, List.of(e3_1, e3_2));
        approvalRepository.save(new ClaimApproval(c3, rahul, ApprovalAction.APPROVED, "Approved by manager Rahul"));

        // --- Claim 4: Amit Verma - Team Dinner & Taxis (SUBMITTED - Contains duplicate candidate) ---
        Claim c4 = createClaimHelper("CLM-2026-00004", "Team Dinner & Airport Ride", amit, ClaimStatus.SUBMITTED,
                LocalDateTime.now().minusDays(1), null, null);
        // Resubmitting the same receipt as e1_2 with slight variation
        ExpenseItem e4_1 = addExpenseHelper(c4, "Uber", LocalDate.now().minusDays(21), ExpenseCategory.TAXI, new BigDecimal("450.00"), "Uber Airport Ride to BLR 450 INR");
        ExpenseItem e4_2 = addExpenseHelper(c4, "Swiggy", LocalDate.now().minusDays(2), ExpenseCategory.MEALS, new BigDecimal("1850.00"), "Swiggy Team Dinner Party 1850 INR 07-Sep-2026");
        saveClaimAndItems(c4, List.of(e4_1, e4_2));

        // --- Claim 5: Sneha Kulkarni - Office Supplies & Cloud (APPROVED - Over limit showcase) ---
        Claim c5 = createClaimHelper("CLM-2026-00005", "Office Supplies & Peripherals", sneha, ClaimStatus.APPROVED,
                LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(1), null);
        ExpenseItem e5_1 = addExpenseHelper(c5, "Amazon Supplies", LocalDate.now().minusDays(5), ExpenseCategory.SUPPLIES, new BigDecimal("11200.00"), "Amazon Business Office Supplies & Adapters ₹11,200");
        saveClaimAndItems(c5, List.of(e5_1));
        approvalRepository.save(new ClaimApproval(c5, rahul, ApprovalAction.APPROVED, "Approved urgent supplies purchase"));

        // --- Claim 6: Rahul Sharma (Manager) - Quarterly Leadership Meet (SUBMITTED) ---
        // Critical test case: Rahul filed this, Rahul CANNOT approve it!
        Claim c6 = createClaimHelper("CLM-2026-00006", "Quarterly Leadership Meet", rahul, ClaimStatus.SUBMITTED,
                LocalDateTime.now().minusDays(1), null, null);
        ExpenseItem e6_1 = addExpenseHelper(c6, "MakeMyTrip", LocalDate.now().minusDays(3), ExpenseCategory.TRAVEL, new BigDecimal("7400.00"), "MakeMyTrip Hotel Stay & Conference room 7400 INR");
        saveClaimAndItems(c6, List.of(e6_1));

        // --- Claim 7: Priya Nair - Draft Claim (DRAFT - for live editing test) ---
        Claim c7 = createClaimHelper("CLM-2026-00007", "Monthly Local Conveyance Draft", priya, ClaimStatus.DRAFT,
                null, null, null);
        ExpenseItem e7_1 = addExpenseHelper(c7, "City Taxi / Auto", LocalDate.now().minusDays(1), ExpenseCategory.TAXI, new BigDecimal("220.00"), "Auto ride from office to metro Rs 220");
        saveClaimAndItems(c7, List.of(e7_1));

        // --- Claim 8: Rohan Gupta - Conference Tickets (REJECTED) ---
        Claim c8 = createClaimHelper("CLM-2026-00008", "Conference Tickets", rohan, ClaimStatus.REJECTED,
                LocalDateTime.now().minusDays(7), null, null);
        ExpenseItem e8_1 = addExpenseHelper(c8, "Travel Booking", LocalDate.now().minusDays(8), ExpenseCategory.TRAVEL, new BigDecimal("4500.00"), "Conference Pass Booking 4500");
        saveClaimAndItems(c8, List.of(e8_1));
        approvalRepository.save(new ClaimApproval(c8, rahul, ApprovalAction.REJECTED, "Missing tax invoice and conference agenda approval. Please resubmit with invoice."));

        // --- Claim 9: Karthik Raja - Client Meet Hyderabad (PAID) ---
        Claim c9 = createClaimHelper("CLM-2026-00009", "Hyderabad Client Meet", karthik, ClaimStatus.PAID,
                LocalDateTime.now().minusDays(15), LocalDateTime.now().minusDays(13), LocalDateTime.now().minusDays(10));
        ExpenseItem e9_1 = addExpenseHelper(c9, "IndiGo Airlines", LocalDate.now().minusDays(16), ExpenseCategory.TRAVEL, new BigDecimal("4200.00"), "IndiGo 6E 541 Hyd Flight 4200 INR");
        ExpenseItem e9_2 = addExpenseHelper(c9, "Chai Point", LocalDate.now().minusDays(15), ExpenseCategory.MEALS, new BigDecimal("280.00"), "Chai Point Snacks and Tea Rs. 280");
        saveClaimAndItems(c9, List.of(e9_1, e9_2));
        approvalRepository.save(new ClaimApproval(c9, vikram, ApprovalAction.APPROVED, "Approved travel"));
        paymentRepository.save(new Payment(c9, anita, "PAY-2026-00002", c9.getTotalAmount(), "Paid via Direct Bank Transfer"));

        // --- Claim 10: Divya Menon - Project Kickoff Meals (SUBMITTED for Vikram) ---
        Claim c10 = createClaimHelper("CLM-2026-00010", "Project Kickoff Team Lunch", divya, ClaimStatus.SUBMITTED,
                LocalDateTime.now().minusDays(2), null, null);
        ExpenseItem e10_1 = addExpenseHelper(c10, "Swiggy", LocalDate.now().minusDays(2), ExpenseCategory.MEALS, new BigDecimal("1850.00"), "Swiggy Team Lunch ₹1850 on 07 Sep");
        saveClaimAndItems(c10, List.of(e10_1));

        // ==========================================
        // 3. Seed Duplicate Alerts with Component Scores
        // ==========================================
        // Alert 1: e4_1 (Uber 450 INR) vs e1_2 (Uber 450 INR)
        DuplicateAlert alert1 = new DuplicateAlert(
                e4_1,
                e1_2,
                new BigDecimal("40.00"), // Exact Amount match
                new BigDecimal("35.00"), // Merchant partial match
                new BigDecimal("20.00"), // Exact same date
                new BigDecimal("95.00")  // Total score
        );
        alert1.setStatus(DuplicateStatus.PENDING_REVIEW);
        duplicateAlertRepository.save(alert1);

        // Alert 2: e10_1 (Swiggy 1850) vs e4_2 (Swiggy 1850)
        DuplicateAlert alert2 = new DuplicateAlert(
                e10_1,
                e4_2,
                new BigDecimal("40.00"), // Exact Amount match
                new BigDecimal("40.00"), // Exact Merchant match
                new BigDecimal("20.00"), // Same date
                new BigDecimal("100.00") // Total score
        );
        alert2.setStatus(DuplicateStatus.PENDING_REVIEW);
        duplicateAlertRepository.save(alert2);
    }

    private Claim createClaimHelper(String claimNumber, String title, User employee, ClaimStatus status,
                                    LocalDateTime submittedAt, LocalDateTime approvedAt, LocalDateTime paidAt) {
        Claim claim = new Claim(claimNumber, title, employee);
        claim.setStatus(status);
        claim.setSubmittedAt(submittedAt);
        claim.setApprovedAt(approvedAt);
        claim.setPaidAt(paidAt);
        return claimRepository.save(claim);
    }

    private ExpenseItem addExpenseHelper(Claim claim, String merchant, LocalDate date, ExpenseCategory category,
                                         BigDecimal amount, String receiptText) {
        return new ExpenseItem(claim, merchant, date, category, amount, receiptText, true);
    }

    private void saveClaimAndItems(Claim claim, List<ExpenseItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        List<ExpenseItem> saved = new ArrayList<>();
        for (ExpenseItem item : items) {
            item.setClaim(claim);
            saved.add(expenseItemRepository.save(item));
            total = total.add(item.getAmount());
        }
        claim.setExpenseItems(saved);
        claim.setTotalAmount(total);
        claimRepository.save(claim);
    }
}
