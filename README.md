# Expense Claims Management System

A full-stack **Expense Claims Management System** built with **Spring Boot 3 (Java 21)**, **Spring Data JPA**, **H2/PostgreSQL**, and a modern, high-performance **Glassmorphism Single Page Application (SPA)**.

Designed to address the real-world operational friction in expense reimbursement: eliminating tedious multi-field form entry, enforcing strict separation of duties and manager self-approval prohibition, detecting spaced-out fuzzy duplicate receipts, and providing real-time financial spend and limit tracking.

---

## ⚡ 10-Second Quick Start

The backend comes pre-packaged with the Maven wrapper and an embedded database in PostgreSQL compatibility mode (`MODE=PostgreSQL`). No external database installation or Docker is required.

### 1. Run Backend & Frontend (Single Command)
```bash
cd backend
./mvnw clean spring-boot:run
```
*(On Windows PowerShell: `.\mvnw.cmd spring-boot:run`)*

### 2. Open the Application
* **Web Dashboard**: [http://localhost:8080/](http://localhost:8080/)
* **Interactive OpenAPI / Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Embedded H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:expense_claims_db;MODE=PostgreSQL`
  - Username: `sa` | Password: *(blank)*

---

## 🎭 Persona Switcher (Instant Demoability)

Rather than forcing interviewers to log in and out with passwords and JWT tokens, the top navigation bar features a **1-Click Persona Switcher** driven by `X-User-Id` / `X-Role` request headers:

| Persona | Name | Role | Monthly Limit | Capabilities to Test |
| :--- | :--- | :--- | :--- | :--- |
| **Staff** | Ram Kumar | `STAFF` | ₹15,000 | Paste receipts, edit drafts, submit claims, view own claims. *(Over limit demo)* |
| **Staff** | Priya Nair | `STAFF` | ₹12,000 | Files local conveyance draft, inspects approval timeline. |
| **Manager** | Rahul Sharma | `MANAGER` | ₹50,000 | Review and sign off on team claims. **Self-approval is strictly blocked.** |
| **Finance** | Anita Desai | `FINANCE` | ₹100,000 | Disburse payouts (terminal state), inspect duplicate alerts, view spend reports. |

---

## 🧩 Architectural Decisions & Key Assumptions

1. **Manager Self-Approval Guardrail**:
   - *Requirement*: Managers spend money too, but a manager must not sign off their own claim.
   - *Implementation*: `AuthorizationService.canApproveOrRejectClaim()` strictly verifies `claim.getEmployee().getId() != approver.getId()`. The UI visibly tags manager-submitted claims and disables approval with the notice `"Self-Approval Prohibited"`.

2. **Immutable Paid Claims**:
   - *Requirement*: A claim that has been paid is finished and should not go backwards.
   - *Implementation*: `WorkflowService.validateTransition()` treats `PAID` as a strict terminal sink state. Any mutation, edit, or reversion attempts throw a `BusinessException` with HTTP 400.

3. **Chosen Implementation Heuristic for Duplicate Detection (40-40-20)**:
   - *Requirement*: People send the same receipt twice (same day or weeks later, typed slightly differently).
   - *Implementation Decision*: This weighting is an independent engineering decision made for the assessment to detect spaced-out duplicate claims deterministically:
     $$\text{Similarity Score} = \text{Amount Score (40\%)} + \text{Merchant Score (40\%)} + \text{Date Score (20\%)}$$
     - **Amount (40%)**: Exact match = 40, within 2% = 30, within 5% = 20.
     - **Merchant (40%)**: Normalized token overlap + Levenshtein distance matching variations (e.g., *"Uber"* vs *"Uber Airport Ride"*).
     - **Date (20%)**: Same date = 20, within 7 days = 15, within 30 days = 10, within 60 days = 5.
     - A score $\ge 80\%$ automatically flags a `DuplicateAlert` with status `PENDING_REVIEW` for Finance review with side-by-side component score breakdown.

4. **Low-Friction Receipt Text Ingestion**:
   - *Requirement*: Nobody wants to fill 6 fields for a ₹200 auto ride. Paste receipt text and preview before submission.
   - *Implementation Decision*: The current system utilizes a deterministic heuristic parsing engine (`ReceiptParsingService`) that tokenizes unstructured text using regex patterns for currency (`₹`, `INR`, `Rs.`), dates (ISO, DD/MM/YYYY, Mon DD YYYY), and auto-classifies categories (`TRAVEL`, `TAXI`, `MEALS`, `SUPPLIES`, `OTHER`). *(Integrating a full multimodal LLM / Cloud Vision OCR pipeline is designed as a future enhancement).*

5. **Auto-Generated Business Identifiers**:
   - Claim numbers are generated server-side in the format `CLM-YYYY-XXXXX`.
   - Finance payment disbursement references are generated server-side as `PAY-YYYY-XXXXX`.

---

## 📋 Assignment Requirement Coverage

| Assignment Requirement | Implementation Strategy & Location | Status |
| :--- | :--- | :---: |
| Staff files claims | Claim creation & item APIs + Staff UI | ✅ |
| Manager reviews team claims | Manager team inbox & pending approvals API | ✅ |
| Manager cannot approve own claim | `AuthorizationService` server-side check | ✅ |
| Paid claim cannot go backwards | `WorkflowService` terminal-state immutability | ✅ |
| Duplicate receipt detection | `DuplicateDetectionService` fuzzy 40/40/20 algorithm | ✅ |
| Monthly spend reporting | `ReportingService` aggregate breakdown APIs | ✅ |
| Category spend breakdown | Category spend report API + Dashboard chart | ✅ |
| Over-limit employee tracking | Over-limit reporting API with budget excess calculation | ✅ |
| Receipt text $\rightarrow$ structured claim | `ReceiptParsingService` heuristic regex engine | ✅ |
| User correction before submission | Editable parsed draft form before submit | ✅ |
| Realistic corporate data | `DataInitializer` corporate hierarchy & claim seed | ✅ |

---

## 🎬 3-Minute Video Walkthrough Script (Demo Order)

To view the complete business story in 3-5 minutes, follow this exact sequence:

1. **Staff (Ram Kumar)** $\rightarrow$ Paste messy receipt text: `UBER Airport Ride 450 INR 14 Aug 2026`
2. **Parser** $\rightarrow$ Automatically tokenizes merchant (*Uber*), amount (*₹450*), category (*TAXI*), date (*14 Aug 2026*).
3. **Correct / Confirm** $\rightarrow$ User reviews parsed fields and clicks "Add Line Item".
4. **Submit Claim** $\rightarrow$ Transition status from `DRAFT` to `SUBMITTED`.
5. **Switch Persona to Manager (Rahul Sharma)** $\rightarrow$ Inspect team pending approvals inbox.
6. **Manager Self-Approval Block** $\rightarrow$ Rahul attempts to approve his own claim (`#6`) $\rightarrow$ System prevents with `"Self-Approval Prohibited"` (`HTTP 403`).
7. **Approve Team Claim** $\rightarrow$ Rahul approves Ram's submitted claim. Status transitions to `APPROVED`.
8. **Switch Persona to Finance (Anita Desai)** $\rightarrow$ Inspect Duplicate Alert inbox.
9. **Deterministic Duplicate Score Breakdown** $\rightarrow$ Review flag showing $40\% + 35\% + 20\% = 95\%$ similarity score.
10. **Pay Approved Claim** $\rightarrow$ Disburse payout with reference `PAY-2026-99001`. Status transitions to `PAID`.
11. **Verify Immutability** $\rightarrow$ Attempt to edit or revert `PAID` claim $\rightarrow$ Blocked as locked terminal state (`HTTP 400`).
12. **Monthly Financial Reports** $\rightarrow$ View aggregate spend by employee and category.
13. **Over-Limit Detection** $\rightarrow$ Flag employees exceeding their monthly limit (e.g., Ram Kumar with excess spend).

---

## 🤖 AI Tools Disclosure

In accordance with the assignment guidelines:
- **Antigravity AI (Pair Programming Assistant)**: Utilized for initial schema design verification, REST API contract drafting, boilerplate code generation, test suite creation, and documentation synthesis.
- **Regex & Heuristic Pattern Matching Engine**: Developed in the backend for receipt text extraction, confidence estimation, and Levenshtein token similarity matching.

---

## 🚀 What I Would Build Next (Given Another Week)

1. **Multimodal OCR Image Pipeline**:
   - Integrate Google Cloud Vision API or Tesseract OCR to allow employees to upload mobile photos/screenshots of physical paper receipts directly.
2. **Push Notifications & Webhooks**:
   - Real-time WebSocket or email notifications alerting managers when a team claim is submitted and notifying employees when reimbursement is disbursed.
3. **Multi-Currency & Exchange Rate Support**:
   - Automated conversion for foreign travel expenses (USD, EUR, GBP) using a live forex API.
4. **Export to ERP & CSV/Excel**:
   - One-click export of monthly financial batches for seamless integration into accounting software (SAP, NetSuite, Tally, Zoho Books).

---

## 🧪 Automated Testing & Verification

Run the automated test suite:
```bash
cd backend
./mvnw test
```

### Verified JUnit Integration Test Suite (7/7 Passed):
- `testManagerCannotApproveOwnClaim`: Proves self-approval attempt throws `BusinessException`.
- `testPaidClaimCannotGoBackwards`: Proves paid claims cannot revert to `APPROVED` or `DRAFT`.
- `testReceiptParsing`: Proves unstructured strings like `"UBER Airport Ride 450 INR 14 Aug 2026"` extract amount, date, merchant, and category.
- `testDuplicateDetection`: Proves identical/fuzzy receipts trigger duplicate alerts with score $\ge 80\%$.
- `testRejectedClaimCanBeEditedAndResubmitted`: Proves end-to-end `SUBMITTED -> REJECTED -> EDIT -> RESUBMIT -> SUBMITTED` lifecycle.
- `testComprehensivePaidClaimImmutability`: Proves all 5 prohibited state mutations on `PAID` claims are blocked.
- `testDeterministicDuplicateScoreComponents`: Proves explicit $40 + 35 + 20 = 95\%$ component score sum.

*(Additionally, `test_all_apis.js` executes 33 live HTTP API endpoint tests against the running server for full contract verification).*

---

## 📂 Project Structure

```
d:\VSP\
├── backend/
│   ├── mvnw / mvnw.cmd
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/vsp/expenseclaims/
│       │   │   ├── config/              # Security filter, CORS, OpenAPI Swagger
│       │   │   ├── controller/          # 25 Action-driven REST Controllers
│       │   │   ├── dto/                 # Strongly-typed Request/Response DTOs
│       │   │   ├── entity/              # 6 JPA Entities & Enums
│       │   │   ├── exception/           # BusinessException & GlobalExceptionHandler
│       │   │   ├── repository/          # Spring Data JPA Repositories & SQL Projections
│       │   │   ├── seed/                # Realistic Corporate Seed Data Initializer
│       │   │   └── service/             # 10 Domain Services & Business Rules
│       │   └── resources/
│       │       ├── application.properties
│       │       └── static/              # Embedded Web Dashboard (HTML, CSS, JS)
│       └── test/                        # Automated JUnit 5 Test Suite
├── frontend/                            # Standalone frontend copy
│   ├── index.html
│   ├── css/style.css
│   └── js/app.js
├── API_TEST_RESULTS.md                  # Comprehensive HTTP execution report
└── README.md                            # Main project documentation & submission guide
```
