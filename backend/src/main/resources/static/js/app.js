// Expense Claims Management System - Interactive Frontend Application

const API_BASE = '/api/v1';

const state = {
  activeUser: {
    id: 4,
    name: 'Ram Kumar',
    email: 'ram@company.com',
    role: 'STAFF',
    monthlyLimit: 15000.00
  },
  allUsers: [],
  currentTab: 'tab-parse',
  pendingRejectClaimId: null
};

// ==========================================
// API Helper with Header-based Persona Context
// ==========================================
async function api(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    'X-User-Id': state.activeUser ? state.activeUser.id.toString() : '4',
    'X-Role': state.activeUser ? state.activeUser.role : 'STAFF',
    ...(options.headers || {})
  };

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers
    });

    if (res.status === 204) {
      return null;
    }

    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      throw new Error(data.message || `Request failed with status ${res.status}`);
    }
    return data;
  } catch (err) {
    showToast(err.message || 'Network request failed', 'error');
    throw err;
  }
}

// ==========================================
// Initialization
// ==========================================
document.addEventListener('DOMContentLoaded', async () => {
  setupTabs();
  setupSampleChips();
  setupEventListeners();
  await loadUsers();
  await refreshDashboard();
  loadCurrentTab();
});

// ==========================================
// User Context & Persona Switcher
// ==========================================
async function loadUsers() {
  try {
    const users = await api('/users/all');
    state.allUsers = users;

    const selector = document.getElementById('userSelector');
    selector.innerHTML = '';

    users.forEach(u => {
      const opt = document.createElement('option');
      opt.value = u.id;
      opt.textContent = `${u.fullName} (${u.role})`;
      if (u.id === state.activeUser.id) opt.selected = true;
      selector.appendChild(opt);
    });

    updateUserUI();
  } catch (err) {
    console.error('Failed to load users', err);
  }
}

function setActiveUser(userId) {
  const found = state.allUsers.find(u => u.id === Number(userId));
  if (found) {
    state.activeUser = {
      id: found.id,
      name: found.fullName,
      email: found.email,
      role: found.role,
      monthlyLimit: found.monthlyLimit
    };
    updateUserUI();
    refreshDashboard();
    loadCurrentTab();
    showToast(`Switched active persona to ${state.activeUser.name} (${state.activeUser.role})`, 'info');
  }
}

function updateUserUI() {
  document.getElementById('activeUserName').textContent = state.activeUser.name;
  document.getElementById('activeUserLimit').textContent = `₹${formatNumber(state.activeUser.monthlyLimit || 0)}`;

  const badge = document.getElementById('currentUserBadge');
  badge.textContent = state.activeUser.role;
  badge.className = `user-badge badge-${state.activeUser.role.toLowerCase()}`;

  const selector = document.getElementById('userSelector');
  if (selector) selector.value = state.activeUser.id;

  // Highlight preset button
  document.querySelectorAll('.btn-preset').forEach(btn => {
    btn.classList.toggle('active', Number(btn.dataset.userId) === state.activeUser.id);
  });

  const myClaimsUserLabel = document.getElementById('myClaimsUserLabel');
  if (myClaimsUserLabel) myClaimsUserLabel.textContent = state.activeUser.name;
}

// ==========================================
// Hero Dashboard Summary
// ==========================================
async function refreshDashboard() {
  try {
    const summary = await api('/dashboard/summary');
    document.getElementById('metricDrafts').textContent = summary.draftClaims;
    document.getElementById('metricSubmitted').textContent = summary.submittedClaims;
    document.getElementById('metricApproved').textContent = summary.approvedClaims;
    document.getElementById('metricPaid').textContent = summary.paidClaims;
    document.getElementById('metricDuplicates').textContent = summary.duplicateAlerts;
    document.getElementById('metricOverLimit').textContent = summary.overLimitCount;
    document.getElementById('metricTotalSpend').textContent = `₹${formatNumber(summary.monthlyTotalSpend)}`;

    // Tab badges
    document.getElementById('badgeApprovals').textContent = summary.submittedClaims;
    document.getElementById('badgeFinance').textContent = summary.approvedClaims;
    document.getElementById('badgeDuplicates').textContent = summary.duplicateAlerts;
  } catch (err) {
    console.error('Failed to refresh dashboard summary', err);
  }
}

// ==========================================
// Tabs Management
// ==========================================
function setupTabs() {
  const tabButtons = document.querySelectorAll('.tab-btn');
  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      tabButtons.forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));

      btn.classList.add('active');
      const tabId = btn.dataset.tab;
      document.getElementById(tabId).classList.add('active');
      state.currentTab = tabId;
      loadCurrentTab();
    });
  });
}

function loadCurrentTab() {
  switch (state.currentTab) {
    case 'tab-my-claims':
      loadMyClaims();
      break;
    case 'tab-approvals':
      loadPendingApprovals();
      break;
    case 'tab-finance':
      loadFinanceQueue();
      break;
    case 'tab-duplicates':
      loadDuplicateAlerts();
      break;
    case 'tab-reports':
      loadReports();
      break;
  }
}

// ==========================================
// Tab 1: Smart Receipt Parser & Ingestion
// ==========================================
function setupSampleChips() {
  document.querySelectorAll('.chip-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.getElementById('receiptInput').value = btn.dataset.sample;
      parseReceiptText();
    });
  });
}

async function parseReceiptText() {
  const text = document.getElementById('receiptInput').value.trim();
  if (!text) {
    showToast('Please enter or paste receipt text first', 'info');
    return;
  }

  try {
    const parsed = await api('/receipts/parse', {
      method: 'POST',
      body: JSON.stringify({ receiptText: text })
    });

    document.getElementById('draftMerchant').value = parsed.merchant || '';
    document.getElementById('draftAmount').value = parsed.amount || '';
    document.getElementById('draftDate').value = parsed.expenseDate || '';
    document.getElementById('draftCategory').value = parsed.category || 'OTHER';

    // Animate confidence meter
    const confidence = parsed.confidence || 75;
    document.getElementById('confidenceFill').style.width = `${confidence}%`;
    document.getElementById('confidenceText').textContent = `${confidence}% Confidence`;

    showToast(`Receipt parsed! Identified ${parsed.merchant} (₹${parsed.amount})`, 'success');
  } catch (err) {
    console.error('Failed to parse receipt', err);
  }
}

async function createClaimWithItem(submitImmediately = true) {
  const title = document.getElementById('draftClaimTitle').value.trim();
  const merchant = document.getElementById('draftMerchant').value.trim();
  const amount = document.getElementById('draftAmount').value;
  const date = document.getElementById('draftDate').value;
  const category = document.getElementById('draftCategory').value;
  const receiptText = document.getElementById('receiptInput').value.trim();

  if (!title) {
    showToast('Please provide a claim title', 'error');
    return;
  }
  if (!merchant || !amount || Number(amount) <= 0 || !date) {
    showToast('Please fill in merchant, valid amount, and expense date', 'error');
    return;
  }

  try {
    // 1. Create Draft Claim
    const claim = await api('/claims', {
      method: 'POST',
      body: JSON.stringify({ title })
    });

    // 2. Add Expense Item
    const item = await api(`/claims/${claim.claimId}/items`, {
      method: 'POST',
      body: JSON.stringify({
        merchantName: merchant,
        amount: Number(amount),
        expenseDate: date,
        category: category,
        receiptText: receiptText
      })
    });

    if (item.duplicateWarning) {
      document.getElementById('duplicateAlertNotice').style.display = 'block';
      showToast(`⚠️ Duplicate Warning: Similarity score ${item.similarityScore}%. Alert flagged!`, 'info');
    }

    // 3. Submit Claim if requested
    if (submitImmediately) {
      await api(`/claims/${claim.claimId}/submit`, { method: 'POST' });
      showToast(`🎉 Claim ${claim.claimNumber} submitted successfully for manager sign-off!`, 'success');
    } else {
      showToast(`Draft claim ${claim.claimNumber} saved. You can edit or submit later.`, 'success');
    }

    // Reset input fields
    document.getElementById('receiptInput').value = '';
    document.getElementById('draftMerchant').value = '';
    document.getElementById('draftAmount').value = '';
    document.getElementById('confidenceFill').style.width = '0%';
    document.getElementById('confidenceText').textContent = '0%';

    await refreshDashboard();
  } catch (err) {
    console.error('Failed to create claim', err);
  }
}

// ==========================================
// Tab 2: My Claims
// ==========================================
async function loadMyClaims() {
  const status = document.getElementById('filterMyClaimsStatus').value;
  const url = status ? `/claims/my?status=${status}` : '/claims/my';

  try {
    const claims = await api(url);
    const tbody = document.getElementById('myClaimsTableBody');
    document.getElementById('badgeMyClaims').textContent = claims.length;

    if (!claims || claims.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2rem;">No claims found for ${state.activeUser.name}.</td></tr>`;
      return;
    }

    tbody.innerHTML = claims.map(c => `
      <tr>
        <td><strong>${c.claimNumber}</strong></td>
        <td>${escapeHtml(c.title)}</td>
        <td><span class="status-pill status-${c.status}">${c.status}</span></td>
        <td>${c.itemCount} items</td>
        <td style="font-weight: 700;">₹${formatNumber(c.totalAmount)}</td>
        <td>${formatDate(c.submittedAt || c.createdAt)}</td>
        <td>
          <div style="display: flex; gap: 0.4rem;">
            <button class="btn btn-secondary btn-sm" onclick="viewClaimDetails(${c.claimId})">View</button>
            ${(c.status === 'DRAFT' || c.status === 'REJECTED') ? `
              <button class="btn btn-primary btn-sm" onclick="submitExistingClaim(${c.claimId})">Submit</button>
              <button class="btn btn-danger btn-sm" onclick="deleteExistingClaim(${c.claimId})">Delete</button>
            ` : ''}
          </div>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    console.error('Failed to load my claims', err);
  }
}

async function submitExistingClaim(claimId) {
  try {
    const res = await api(`/claims/${claimId}/submit`, { method: 'POST' });
    showToast(`Claim ${res.claimNumber} submitted for manager review!`, 'success');
    loadMyClaims();
    refreshDashboard();
  } catch (err) {}
}

async function deleteExistingClaim(claimId) {
  if (!confirm('Are you sure you want to delete this draft claim?')) return;
  try {
    await api(`/claims/${claimId}`, { method: 'DELETE' });
    showToast('Draft claim deleted successfully', 'success');
    loadMyClaims();
    refreshDashboard();
  } catch (err) {}
}

// ==========================================
// Tab 3: Manager Approvals
// ==========================================
async function loadPendingApprovals() {
  const tbody = document.getElementById('approvalsTableBody');

  if (state.activeUser.role !== 'MANAGER') {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; color: #fbbf24; padding: 2rem;">
          ⚠️ You are currently viewing as <strong>${state.activeUser.role}</strong>. Switch to <strong>Rahul (Manager)</strong> using the top bar to approve team claims.
        </td>
      </tr>
    `;
    return;
  }

  try {
    const claims = await api('/claims/pending-approvals');
    document.getElementById('badgeApprovals').textContent = claims.length;

    if (!claims || claims.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2rem;">No pending approvals in your queue. Great job!</td></tr>`;
      return;
    }

    tbody.innerHTML = claims.map(c => {
      // Rule check: Manager cannot approve their own claim!
      const isSelfClaim = (c.employeeId === state.activeUser.id);
      return `
        <tr>
          <td><strong>${c.claimNumber}</strong></td>
          <td>${c.employeeName} ${isSelfClaim ? '<span class="status-pill status-REJECTED" style="font-size:0.65rem;">YOU</span>' : ''}</td>
          <td>${escapeHtml(c.title)}</td>
          <td>${c.itemCount} items</td>
          <td style="font-weight: 700;">₹${formatNumber(c.totalAmount)}</td>
          <td>${formatDate(c.submittedAt)}</td>
          <td>
            <div style="display: flex; gap: 0.4rem;">
              <button class="btn btn-secondary btn-sm" onclick="viewClaimDetails(${c.claimId})">Inspect</button>
              ${isSelfClaim ? `
                <button class="btn btn-secondary btn-sm" disabled title="Manager cannot sign off their own claim">Self-Approval Prohibited</button>
              ` : `
                <button class="btn btn-success btn-sm" onclick="approveClaim(${c.claimId})">Sign Off</button>
                <button class="btn btn-danger btn-sm" onclick="openRejectModal(${c.claimId})">Reject</button>
              `}
            </div>
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    console.error('Failed to load pending approvals', err);
  }
}

async function approveClaim(claimId) {
  try {
    const res = await api(`/claims/${claimId}/approve`, {
      method: 'POST',
      body: JSON.stringify({ comments: `Approved by manager ${state.activeUser.name}` })
    });
    showToast(`Claim ${res.claimNumber} signed off and forwarded to Finance payout queue!`, 'success');
    loadPendingApprovals();
    refreshDashboard();
  } catch (err) {}
}

function openRejectModal(claimId) {
  state.pendingRejectClaimId = claimId;
  document.getElementById('rejectionReasonInput').value = '';
  document.getElementById('rejectionModal').classList.add('active');
}

async function confirmReject() {
  const reason = document.getElementById('rejectionReasonInput').value.trim();
  if (!reason) {
    showToast('Please state a reason for rejection', 'error');
    return;
  }

  try {
    const res = await api(`/claims/${state.pendingRejectClaimId}/reject`, {
      method: 'POST',
      body: JSON.stringify({ reason })
    });
    closeAllModals();
    showToast(`Claim ${res.claimNumber} rejected with reason sent to employee.`, 'info');
    loadPendingApprovals();
    refreshDashboard();
  } catch (err) {}
}

// ==========================================
// Tab 4: Finance Payouts
// ==========================================
async function loadFinanceQueue() {
  const tbody = document.getElementById('financeTableBody');

  if (state.activeUser.role !== 'FINANCE') {
    tbody.innerHTML = `
      <tr>
        <td colspan="6" style="text-align: center; color: #fbbf24; padding: 2rem;">
          ⚠️ You are currently viewing as <strong>${state.activeUser.role}</strong>. Switch to <strong>Anita (Finance)</strong> using the top bar to process payouts.
        </td>
      </tr>
    `;
    return;
  }

  try {
    const claims = await api('/finance/approved-claims');
    document.getElementById('badgeFinance').textContent = claims.length;

    if (!claims || claims.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No approved claims pending reimbursement payout.</td></tr>`;
      return;
    }

    tbody.innerHTML = claims.map(c => `
      <tr>
        <td><strong>${c.claimNumber}</strong></td>
        <td>${c.employeeName}</td>
        <td>${escapeHtml(c.title)}</td>
        <td style="font-weight: 700; color: #34d399;">₹${formatNumber(c.totalAmount)}</td>
        <td>${formatDate(c.approvedAt)}</td>
        <td>
          <div style="display: flex; gap: 0.5rem;">
            <button class="btn btn-secondary btn-sm" onclick="viewClaimDetails(${c.claimId})">Inspect</button>
            <button class="btn btn-primary btn-sm" onclick="disbursePayout(${c.claimId})">
              <span>💸</span> Disburse Payout
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    console.error('Failed to load finance queue', err);
  }
}

async function disbursePayout(claimId) {
  try {
    const payment = await api(`/claims/${claimId}/pay`, {
      method: 'POST',
      body: JSON.stringify({ remarks: 'Reimbursement disbursed via corporate bank direct deposit.' })
    });
    showToast(`💰 Payout processed! Payment Reference: ${payment.paymentReference}. Claim is now PAID and locked.`, 'success');
    loadFinanceQueue();
    refreshDashboard();
  } catch (err) {}
}

// ==========================================
// Tab 5: Duplicate Detection Center
// ==========================================
async function loadDuplicateAlerts() {
  const container = document.getElementById('duplicateAlertsList');

  if (state.activeUser.role !== 'FINANCE') {
    container.innerHTML = `
      <div style="text-align: center; color: #fbbf24; padding: 2rem; background: var(--bg-card); border-radius: 12px; border: 1px solid var(--border-color);">
        ⚠️ Duplicate alerts can only be reviewed by Finance. Switch to <strong>Anita (Finance)</strong> using the top bar to inspect score breakdowns and decide on alerts.
      </div>
    `;
    return;
  }

  try {
    const alerts = await api('/duplicates');
    document.getElementById('badgeDuplicates').textContent = alerts.filter(a => a.status === 'PENDING_REVIEW').length;

    if (!alerts || alerts.length === 0) {
      container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 2rem;">No duplicate receipt alerts recorded.</div>`;
      return;
    }

    container.innerHTML = alerts.map(a => `
      <div class="glass-card dup-alert-card">
        <div class="dup-header">
          <div>
            <strong style="color: #fbbf24; font-size: 1rem;">Duplicate Alert #${a.id}</strong>
            <span class="status-pill status-${a.status === 'PENDING_REVIEW' ? 'SUBMITTED' : (a.status === 'CONFIRMED_DUPLICATE' ? 'REJECTED' : 'APPROVED')}" style="margin-left: 0.5rem;">
              ${a.status.replace('_', ' ')}
            </span>
          </div>
          <span style="font-size: 0.75rem; color: var(--text-muted);">Detected: ${formatDate(a.detectedAt)}</span>
        </div>

        <div class="score-breakdown-row">
          <span>Overall Similarity: <strong class="score-pill" style="color: #f59e0b;">${a.similarityScore}%</strong></span>
          <span>Amount Match: <strong class="score-pill">${a.amountScore} / 40</strong></span>
          <span>Merchant Match: <strong class="score-pill">${a.merchantScore} / 40</strong></span>
          <span>Date Proximity: <strong class="score-pill">${a.dateScore} / 20</strong></span>
          <button class="btn btn-secondary btn-sm" style="margin-left: auto; padding: 2px 8px;" onclick='showDupScoreBreakdown(${JSON.stringify(a).replace(/'/g, "&apos;")})'>ℹ️ Why flagged?</button>
        </div>

        <div class="dup-comparison-grid">
          <!-- Newly Filed Item -->
          <div class="dup-item-box">
            <span style="font-size: 0.72rem; color: #38bdf8; font-weight: 700; text-transform: uppercase;">Newly Submitted Receipt (Item #${a.expenseItemId})</span>
            <div style="font-size: 1.1rem; font-weight: 800; color: #fff; margin: 4px 0;">₹${formatNumber(a.expenseItemAmount)}</div>
            <div style="font-size: 0.85rem; font-weight: 600;">${a.expenseItemMerchant}</div>
            <div style="font-size: 0.75rem; color: var(--text-secondary);">Date: ${a.expenseItemDate}</div>
            <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 4px;">Employee: ${a.expenseItemEmployee || 'N/A'} (Claim #${a.expenseItemClaimId})</div>
          </div>

          <div class="dup-vs-divider">VS</div>

          <!-- Previously Matched Item -->
          <div class="dup-item-box">
            <span style="font-size: 0.72rem; color: #c084fc; font-weight: 700; text-transform: uppercase;">Existing In System (Item #${a.matchedExpenseItemId})</span>
            <div style="font-size: 1.1rem; font-weight: 800; color: #fff; margin: 4px 0;">₹${formatNumber(a.matchedExpenseItemAmount)}</div>
            <div style="font-size: 0.85rem; font-weight: 600;">${a.matchedExpenseItemMerchant}</div>
            <div style="font-size: 0.75rem; color: var(--text-secondary);">Date: ${a.matchedExpenseItemDate}</div>
            <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 4px;">Employee: ${a.matchedExpenseItemEmployee || 'N/A'} (Claim #${a.matchedExpenseItemClaimId})</div>
          </div>
        </div>

        ${a.status === 'PENDING_REVIEW' ? `
          <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
            <button class="btn btn-danger btn-sm" onclick="reviewDuplicateAlert(${a.id}, 'CONFIRMED_DUPLICATE')">
              ❌ Confirm Duplicate (Block Payment)
            </button>
            <button class="btn btn-secondary btn-sm" onclick="reviewDuplicateAlert(${a.id}, 'FALSE_POSITIVE')">
              ✅ Mark False Positive (Allow)
            </button>
          </div>
        ` : `
          <div style="font-size: 0.8rem; color: var(--text-muted); text-align: right;">
            Resolved as: <strong>${a.status}</strong>
          </div>
        `}
      </div>
    `).join('');
  } catch (err) {
    console.error('Failed to load duplicates', err);
  }
}

async function reviewDuplicateAlert(alertId, decision) {
  try {
    await api(`/duplicates/${alertId}/review`, {
      method: 'POST',
      body: JSON.stringify({ decision })
    });
    showToast(`Duplicate alert #${alertId} marked as ${decision}`, 'success');
    loadDuplicateAlerts();
    refreshDashboard();
  } catch (err) {}
}

function showDupScoreBreakdown(alert) {
  const modal = document.getElementById('dupBreakdownModal');
  const body = document.getElementById('modalDupBreakdownBody');

  body.innerHTML = `
    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1rem;">
      The duplicate detection engine calculates a weighted similarity score using the frozen <strong>40-40-20 formula</strong>:
    </p>

    <div style="display: flex; flex-direction: column; gap: 0.75rem;">
      <div style="background: rgba(15,23,42,0.6); padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--border-color); display: flex; justify-content: space-between;">
        <div>
          <strong>1. Amount Match (40% Weight)</strong>
          <p style="font-size: 0.75rem; color: var(--text-muted);">₹${alert.expenseItemAmount} vs ₹${alert.matchedExpenseItemAmount}</p>
        </div>
        <span style="font-weight: 800; color: #38bdf8;">${alert.amountScore} / 40.00</span>
      </div>

      <div style="background: rgba(15,23,42,0.6); padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--border-color); display: flex; justify-content: space-between;">
        <div>
          <strong>2. Merchant Name Similarity (40% Weight)</strong>
          <p style="font-size: 0.75rem; color: var(--text-muted);">${alert.expenseItemMerchant} vs ${alert.matchedExpenseItemMerchant}</p>
        </div>
        <span style="font-weight: 800; color: #a855f7;">${alert.merchantScore} / 40.00</span>
      </div>

      <div style="background: rgba(15,23,42,0.6); padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--border-color); display: flex; justify-content: space-between;">
        <div>
          <strong>3. Expense Date Proximity (20% Weight)</strong>
          <p style="font-size: 0.75rem; color: var(--text-muted);">${alert.expenseItemDate} vs ${alert.matchedExpenseItemDate}</p>
        </div>
        <span style="font-weight: 800; color: #34d399;">${alert.dateScore} / 20.00</span>
      </div>

      <div style="background: rgba(245,158,11,0.15); padding: 0.85rem 1rem; border-radius: 8px; border: 1px solid #f59e0b; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <strong style="color: #fbbf24;">Total Composite Score</strong>
          <p style="font-size: 0.75rem; color: var(--text-secondary);">Threshold for flag: &ge; 80%</p>
        </div>
        <span style="font-size: 1.3rem; font-weight: 800; color: #fbbf24;">${alert.similarityScore}%</span>
      </div>
    </div>
  `;

  modal.classList.add('active');
}

// ==========================================
// Tab 6: Finance Reports & Compliance
// ==========================================
async function loadReports() {
  if (state.activeUser.role !== 'FINANCE') {
    document.getElementById('tab-reports').innerHTML = `
      <div style="text-align: center; color: #fbbf24; padding: 2rem; background: var(--bg-card); border-radius: 12px; border: 1px solid var(--border-color);">
        ⚠️ Company financial reports are restricted to Finance personnel. Switch to <strong>Anita (Finance)</strong> using the top bar to inspect spend and limit breaches.
      </div>
    `;
    return;
  }

  try {
    const [categories, employees, overLimits] = await Promise.all([
      api('/reports/category-spend'),
      api('/reports/employee-spend'),
      api('/reports/over-limit')
    ]);

    // 1. Category Spend Bars
    const catContainer = document.getElementById('categorySpendBars');
    const maxCat = Math.max(...categories.map(c => c.total), 1);

    catContainer.innerHTML = categories.map(c => {
      const pct = Math.min((c.total / maxCat) * 100, 100);
      return `
        <div>
          <div style="display: flex; justify-content: space-between; font-size: 0.8rem; margin-bottom: 3px;">
            <span class="category-tag">${c.category}</span>
            <strong>₹${formatNumber(c.total)}</strong>
          </div>
          <div style="height: 6px; background: rgba(255,255,255,0.08); border-radius: 999px; overflow: hidden;">
            <div style="height: 100%; width: ${pct}%; background: var(--accent-gradient); border-radius: 999px;"></div>
          </div>
        </div>
      `;
    }).join('');

    // 2. Employee Spend Table
    const empTbody = document.getElementById('employeeSpendTableBody');
    empTbody.innerHTML = employees.map(e => `
      <tr>
        <td><strong>${e.employee}</strong><br><span style="font-size:0.7rem; color:var(--text-muted);">${e.email}</span></td>
        <td style="font-weight: 700; color: #fff;">₹${formatNumber(e.total)}</td>
      </tr>
    `).join('');

    // 3. Over Limit Banner & Table
    const overLimitBanner = document.getElementById('overLimitContainer');
    const overLimitTbody = document.getElementById('overLimitTableBody');

    if (overLimits && overLimits.length > 0) {
      overLimitBanner.style.display = 'block';
      document.getElementById('overLimitSummaryText').textContent =
        `${overLimits.length} employee(s) have exceeded their monthly expense budget limit. Review pending claims carefully before signing off.`;

      overLimitTbody.innerHTML = overLimits.map(o => `
        <tr>
          <td><strong>${o.employee}</strong></td>
          <td style="color: var(--text-muted);">${o.email}</td>
          <td>₹${formatNumber(o.limit)}</td>
          <td style="font-weight: 700; color: #ef4444;">₹${formatNumber(o.spent)}</td>
          <td style="font-weight: 700; color: #f87171;">+₹${formatNumber(o.excess)}</td>
          <td><span class="status-pill status-REJECTED">OVER BUDGET</span></td>
        </tr>
      `).join('');
    } else {
      overLimitBanner.style.display = 'none';
      overLimitTbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: #34d399; padding: 1rem;">All active employees are within their monthly limits.</td></tr>`;
    }
  } catch (err) {
    console.error('Failed to load reports', err);
  }
}

// ==========================================
// Claim Details Modal
// ==========================================
async function viewClaimDetails(claimId) {
  try {
    const claim = await api(`/claims/${claimId}`);
    const modal = document.getElementById('claimDetailModal');
    const title = document.getElementById('modalClaimTitle');
    const body = document.getElementById('modalClaimBody');

    title.innerHTML = `${claim.claimNumber} &mdash; ${escapeHtml(claim.title)}`;

    body.innerHTML = `
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem; margin-bottom: 1.25rem; font-size: 0.85rem; background: rgba(15,23,42,0.6); padding: 1rem; border-radius: 8px;">
        <div>Employee: <strong>${claim.employeeName}</strong></div>
        <div>Status: <span class="status-pill status-${claim.status}">${claim.status}</span></div>
        <div>Total Amount: <strong style="font-size: 1.1rem; color: #fff;">₹${formatNumber(claim.totalAmount)}</strong></div>
        <div>Created: <strong>${formatDate(claim.createdAt)}</strong></div>
        ${claim.payment ? `<div style="grid-column: 1 / -1; color: #34d399;">Payment Reference: <strong>${claim.payment.paymentReference}</strong> (Paid on ${formatDate(claim.payment.paymentDate)})</div>` : ''}
      </div>

      <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 0.5rem;">Expense Line Items (${claim.items.length})</h4>
      <div class="table-container" style="margin-bottom: 1.25rem;">
        <table class="custom-table" style="font-size: 0.8rem;">
          <thead>
            <tr>
              <th>Merchant</th>
              <th>Category</th>
              <th>Date</th>
              <th>Amount</th>
              <th>Receipt Text</th>
            </tr>
          </thead>
          <tbody>
            ${claim.items.map(item => `
              <tr>
                <td><strong>${item.merchantName}</strong></td>
                <td><span class="category-tag">${item.category}</span></td>
                <td>${item.expenseDate}</td>
                <td style="font-weight: 700;">₹${formatNumber(item.amount)}</td>
                <td style="font-size: 0.72rem; color: var(--text-muted);">${escapeHtml(item.receiptText || 'N/A')}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>

      ${claim.approvals && claim.approvals.length > 0 ? `
        <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 0.5rem;">Approval / Audit History</h4>
        <div style="display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.8rem;">
          ${claim.approvals.map(app => `
            <div style="background: rgba(15,23,42,0.4); padding: 0.6rem 0.85rem; border-radius: 6px; border-left: 3px solid ${app.action === 'APPROVED' ? '#34d399' : '#f87171'};">
              <strong>${app.action}</strong> by ${app.approverName} on ${formatDate(app.actionTime)}
              ${app.comments ? `<p style="color: var(--text-secondary); margin-top: 2px;">&ldquo;${escapeHtml(app.comments)}&rdquo;</p>` : ''}
            </div>
          `).join('')}
        </div>
      ` : ''}
    `;

    modal.classList.add('active');
  } catch (err) {
    console.error('Failed to view claim details', err);
  }
}

// ==========================================
// Event Listeners
// ==========================================
function setupEventListeners() {
  // Role selector
  document.getElementById('userSelector').addEventListener('change', (e) => {
    setActiveUser(e.target.value);
  });

  // Preset buttons
  document.querySelectorAll('.btn-preset').forEach(btn => {
    btn.addEventListener('click', () => {
      setActiveUser(btn.dataset.userId);
    });
  });

  // Receipt parsing buttons
  document.getElementById('btnParseReceipt').addEventListener('click', parseReceiptText);
  document.getElementById('btnClearReceipt').addEventListener('click', () => {
    document.getElementById('receiptInput').value = '';
    document.getElementById('confidenceFill').style.width = '0%';
    document.getElementById('confidenceText').textContent = '0%';
  });

  // Create & Submit / Save draft buttons
  document.getElementById('btnCreateAndSubmit').addEventListener('click', () => createClaimWithItem(true));
  document.getElementById('btnSaveDraftOnly').addEventListener('click', () => createClaimWithItem(false));

  // Refresh buttons
  document.getElementById('btnRefreshMyClaims').addEventListener('click', loadMyClaims);
  document.getElementById('filterMyClaimsStatus').addEventListener('change', loadMyClaims);
  document.getElementById('btnRefreshApprovals').addEventListener('click', loadPendingApprovals);
  document.getElementById('btnRefreshFinance').addEventListener('click', loadFinanceQueue);
  document.getElementById('btnRefreshDuplicates').addEventListener('click', loadDuplicateAlerts);
  document.getElementById('btnRefreshReports').addEventListener('click', loadReports);

  // Modal close buttons
  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', closeAllModals);
  });

  // Rejection confirmation
  document.getElementById('btnConfirmReject').addEventListener('click', confirmReject);
}

function closeAllModals() {
  document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
  state.pendingRejectClaimId = null;
}

// ==========================================
// Utilities
// ==========================================
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span>${type === 'success' ? '✅' : (type === 'error' ? '❌' : 'ℹ️')}</span>
    <span>${message}</span>
  `;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function formatNumber(num) {
  return Number(num).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  } catch (e) {
    return dateStr;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>"']/g, m => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[m]));
}
