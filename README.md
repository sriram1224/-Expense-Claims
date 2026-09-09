# Expense Claims Management System

A full-stack enterprise Expense Claims Management System built with **Spring Boot 3 (Java 21)**, **Spring Data JPA**, **H2/PostgreSQL**, and an interactive single-page application dashboard.

This application automates corporate expense workflows, eliminates manual data entry through heuristic receipt text parsing, enforces separation of duties (manager self-approval prevention), detects spaced-out duplicate claims using a multi-factor similarity algorithm, and provides end-of-month financial spend analytics.

---

## 🌐 Live Application & Video Walkthrough

The application is deployed live and ready for testing:
* **Live Web Dashboard**: [https://expense-claims-5sju.onrender.com](https://expense-claims-5sju.onrender.com)
* **Live OpenAPI / Swagger UI Documentation**: [https://expense-claims-5sju.onrender.com/swagger-ui/index.html](https://expense-claims-5sju.onrender.com/swagger-ui/index.html)
* **Live Dashboard Metrics Endpoint**: `https://expense-claims-5sju.onrender.com/api/v1/dashboard/summary`
* **🎥 Video Walkthrough (Google Drive)**: [Watch 3–5 Min Demonstration Video](https://drive.google.com/file/d/1mJ6GAqIpiB3rB2EELYhdwNdeBVkVjwL6/view?usp=sharing)

---

## ⚡ How to Run Locally

### System Requirements
* Java Development Kit (JDK) 21 or higher
* Internet connection (for initial Maven dependency download)

### 1. Single Command Launch
The backend includes the Maven Wrapper (`mvnw` / `mvnw.cmd`) and packages the embedded static frontend dashboard automatically.

**On Linux / macOS:**
```bash
cd backend
./mvnw clean spring-boot:run
```

**On Windows (PowerShell / CMD):**
```powershell
cd backend
.\mvnw.cmd clean spring-boot:run
```

### 2. Access Points
Once the application starts, open your browser to access:
* **Web Dashboard**: [http://localhost:8080/](http://localhost:8080/)
* **OpenAPI / Swagger UI Documentation**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Embedded H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  * JDBC URL: `jdbc:h2:mem:expense_claims_db;MODE=PostgreSQL`
  * Username: `sa`
  * Password: *(blank)*

---

## ☁️ Cloud Deployment (Render)

The repository includes a multi-stage `Dockerfile` that compiles the application inside Docker on Render without needing local `target/` build artifacts.

### Render Setup (3 Steps):
1. Go to [Render Dashboard](https://dashboard.render.com/) $\rightarrow$ **New** $\rightarrow$ **Web Service**.
2. Connect your GitHub repository `https://github.com/sriram1224/-Expense-Claims.git`.
3. Configure settings:
   * **Name**: `expense-claims`
   * **Language**: `Docker`
   * **Dockerfile Path**: `backend/Dockerfile` (or `Dockerfile`)
   * **Instance Type**: Free

*Render automatically sets `PORT=10000` and binds the server address to `0.0.0.0`.*

---

## 👥 Personas & Data Modeling

The system initializes real-world corporate seed data (`DataInitializer.java`) with three distinct user roles:

| User ID | Persona | Role | Department / Manager | Monthly Budget | Target Test Scenarios |
| :---: | :--- | :---: | :--- | :---: | :--- |
| **4** | **Ram Kumar** | `STAFF` | Engineering (Reports to Rahul) | ₹15,000 | Ingest receipts, submit claims, test over-limit reporting |
| **5** | **Priya Nair** | `STAFF` | Operations (Reports to Rahul) | ₹12,000 | File local conveyance, track unpaid claim lifecycle |
| **1** | **Rahul Sharma** | `MANAGER` | Engineering Lead | ₹50,000 | Sign off team claims, **verify self-approval prohibition (Claim #6)** |
| **3** | **Anita Desai** | `FINANCE` | Finance & Treasury | ₹100,000 | Disburse payouts, review duplicate alerts, generate monthly reports |

> **Header-Based Context Validation**: To enable rapid testing across personas without forcing cumbersome auth flows during evaluations, requests accept `X-User-Id` headers. Server-side authorization (`UserContextFilter`) authoritatively resolves the authenticated user role from the database, preventing header spoofing.

---

## 🧠 Architectural Decisions & Assumptions

### 1. Manager Self-Approval Guardrail
* **Problem**: Managers file expenses too, but authorizing their own payouts creates severe financial control compliance issues.
* **Decision**: `AuthorizationService` explicitly enforces `claim.getEmployee().getId() != approver.getId()`. If a manager attempts to sign off on their own submission, the system returns `HTTP 403 Forbidden`.

### 2. Terminal Paid-State Immutability
* **Problem**: Settled claims must not be reverted to draft, re-approved, edited, or paid twice.
* **Decision**: `WorkflowService` implements state machine validation where `PAID` is a terminal sink state. Any mutation request (`PUT`, `DELETE`, re-approval, or duplicate payout) on a settled claim returns `HTTP 400 Bad Request`.

### 3. Spaced-Out Duplicate Detection Heuristic (40-40-20)
* **Problem**: Employees submit duplicate receipts weeks apart with minor formatting differences (e.g., *"Uber Taxi"* vs *"Uber Airport Ride"*).
* **Decision**: The system evaluates historical expense items using a multi-factor scoring formula:
  $$\text{Similarity Score} = \text{Amount (40\%)} + \text{Merchant (40\%)} + \text{Date Proximity (20\%)}$$
  * **Amount (40%)**: Exact match = 40; $\le 2\%$ variance = 30; $\le 5\%$ variance = 20.
  * **Merchant (40%)**: Token overlap & Levenshtein distance matching.
  * **Date (20%)**: Same day = 20; within 7 days = 15; within 30 days = 10; within 60 days = 5.
  * Items scoring $\ge 80\%$ flag a `DuplicateAlert` record in state `PENDING_REVIEW` for Finance audit.

### 4. Low-Friction Receipt Text Parsing
* **Problem**: Manual multi-field form entry creates friction for simple receipts.
* **Decision**: `ReceiptParsingService` applies regex tokenization to unstructured text strings (extracting merchant names, INR/Rs currency amounts, dates in multiple formats, and expense categories). Parsed data auto-populates a draft form allowing employees to verify and correct details prior to submission.

---

## 🤖 AI Tools Disclosure

In accordance with assignment guidelines:

1. **Antigravity AI (Agentic Coding Assistant)**:
   * Used for initial schema design verification, REST DTO generation, boilerplate service creation, JUnit test suite generation, and documentation drafting.
2. **Regex & Heuristic String Matcher**:
   * Custom tokenization and Levenshtein similarity routines implemented within the application core for receipt parsing and duplicate detection.

---

## 🔮 What I Would Build Next (Given Another Week)

1. **Multimodal Optical Character Recognition (OCR)**:
   * Integrate Tesseract OCR or Google Cloud Vision API to accept direct image uploads (JPG/PNG receipts and screenshots) alongside raw text.
2. **Asynchronous Webhooks & Email Notifications**:
   * Implement Spring Event Listeners to notify managers when team claims are submitted and alert employees upon payout disbursement.
3. **Multi-Currency & Real-Time Exchange Rates**:
   * Support foreign currency claims (USD, EUR, GBP) with automatic forex conversion via open exchange rate APIs.
4. **ERP / Accounting Export Integration**:
   * Add CSV/JSON batch exports formatted for seamless ingestion into ERP platforms (SAP, Zoho Books, Tally).

---

## 🧪 Automated Testing & Verification

### Run Integration Tests
```bash
cd backend
.\mvnw.cmd test
```

### Key Verified JUnit Test Cases (`ExpenseClaimsApplicationTests.java`)
* `testManagerCannotApproveOwnClaim`: Verifies manager self-approval throws `BusinessException` and returns `403`.
* `testPaidClaimCannotGoBackwards`: Verifies `PAID` state transitions back to `APPROVED` or `DRAFT` are rejected.
* `testComprehensivePaidClaimImmutability`: Verifies all 5 prohibited state mutations on settled claims are blocked.
* `testReceiptParsing`: Verifies unstructured string extraction for merchant, amount, category, and date.
* `testDuplicateDetection`: Verifies identical and fuzzy receipts generate duplicate warnings with score $\ge 80\%$.
* `testDeterministicDuplicateScoreComponents`: Verifies component score summation ($40 + 35 + 20 = 95\%$).
* `testRejectedClaimCanBeEditedAndResubmitted`: Verifies full `SUBMITTED -> REJECTED -> EDIT -> RESUBMIT` lifecycle.

---

## 📁 Repository Structure

```text
.
├── backend/
│   ├── mvnw / mvnw.cmd              # Maven wrapper scripts
│   ├── pom.xml                       # Maven build specification & dependencies
│   └── src/
│       ├── main/
│       │   ├── java/com/vsp/expenseclaims/
│       │   │   ├── config/           # Security, UserContextFilter, Swagger OpenAPI
│       │   │   ├── controller/       # REST Controllers (Claims, Approvals, Finance, Reports)
│       │   │   ├── dto/              # Request & Response DTOs
│       │   │   ├── entity/           # JPA Domain Entities (User, Claim, ExpenseItem, Payment, etc.)
│       │   │   ├── exception/        # Exception handlers & BusinessException
│       │   │   ├── repository/       # Spring Data JPA Repositories
│       │   │   ├── seed/             # Corporate DataInitializer
│       │   │   └── service/          # Core Business Services & Domain Rules
│       │   └── resources/
│       │       ├── application.properties
│       │       └── static/           # Single Page Application Dashboard (HTML/CSS/JS)
│       └── test/                     # JUnit 5 Integration Test Suite
├── frontend/                         # Standalone Single Page Application frontend source
├── API_TEST_RESULTS.md               # 33-Endpoint HTTP API execution report
├── README.md                         # Project documentation
└── .gitignore                        # Git exclusion rules
```
