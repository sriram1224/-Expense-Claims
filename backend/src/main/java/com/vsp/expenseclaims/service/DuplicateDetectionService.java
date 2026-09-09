package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.dto.DuplicateAlertResponse;
import com.vsp.expenseclaims.entity.DuplicateAlert;
import com.vsp.expenseclaims.entity.ExpenseItem;
import com.vsp.expenseclaims.entity.enums.DuplicateStatus;
import com.vsp.expenseclaims.exception.BusinessException;
import com.vsp.expenseclaims.repository.DuplicateAlertRepository;
import com.vsp.expenseclaims.repository.ExpenseItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DuplicateDetectionService {

    private final DuplicateAlertRepository duplicateAlertRepository;
    private final ExpenseItemRepository expenseItemRepository;

    public DuplicateDetectionService(DuplicateAlertRepository duplicateAlertRepository, ExpenseItemRepository expenseItemRepository) {
        this.duplicateAlertRepository = duplicateAlertRepository;
        this.expenseItemRepository = expenseItemRepository;
    }

    public record DuplicateEvaluation(boolean isDuplicate, BigDecimal similarityScore, BigDecimal amountScore, BigDecimal merchantScore, BigDecimal dateScore, ExpenseItem matchedItem) {}

    @Transactional
    public DuplicateEvaluation evaluateAndFlag(ExpenseItem newItem) {
        List<ExpenseItem> existingItems = expenseItemRepository.findCandidateItemsForDuplicateCheck(
                newItem.getId() != null ? newItem.getId() : -1L);

        BigDecimal highestScore = BigDecimal.ZERO;
        DuplicateEvaluation bestMatch = new DuplicateEvaluation(false, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);

        for (ExpenseItem candidate : existingItems) {
            BigDecimal amountScore = calculateAmountScore(newItem.getAmount(), candidate.getAmount());
            BigDecimal merchantScore = calculateMerchantScore(newItem.getMerchantName(), candidate.getMerchantName());
            BigDecimal dateScore = calculateDateScore(newItem.getExpenseDate(), candidate.getExpenseDate());

            BigDecimal totalScore = amountScore.add(merchantScore).add(dateScore);

            if (totalScore.compareTo(highestScore) > 0) {
                highestScore = totalScore;
                boolean isDup = totalScore.compareTo(new BigDecimal("80")) >= 0;
                bestMatch = new DuplicateEvaluation(isDup, totalScore, amountScore, merchantScore, dateScore, candidate);
            }
        }

        if (bestMatch.isDuplicate() && newItem.getId() != null && bestMatch.matchedItem() != null) {
            DuplicateAlert alert = new DuplicateAlert(
                    newItem,
                    bestMatch.matchedItem(),
                    bestMatch.amountScore(),
                    bestMatch.merchantScore(),
                    bestMatch.dateScore(),
                    bestMatch.similarityScore()
            );
            duplicateAlertRepository.save(alert);
        }

        return bestMatch;
    }

    private BigDecimal calculateAmountScore(BigDecimal a1, BigDecimal a2) {
        if (a1 == null || a2 == null) return BigDecimal.ZERO;
        if (a1.compareTo(a2) == 0) {
            return new BigDecimal("40.00");
        }
        BigDecimal diff = a1.subtract(a2).abs();
        BigDecimal maxVal = a1.max(a2);
        if (maxVal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal percentDiff = diff.divide(maxVal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        if (percentDiff.compareTo(new BigDecimal("2")) <= 0) {
            return new BigDecimal("30.00");
        } else if (percentDiff.compareTo(new BigDecimal("5")) <= 0) {
            return new BigDecimal("20.00");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateMerchantScore(String m1, String m2) {
        if (m1 == null || m2 == null) return BigDecimal.ZERO;
        String s1 = m1.trim().toLowerCase(Locale.ROOT);
        String s2 = m2.trim().toLowerCase(Locale.ROOT);

        if (s1.equals(s2)) {
            return new BigDecimal("40.00");
        }
        if (s1.contains(s2) || s2.contains(s1)) {
            return new BigDecimal("35.00");
        }

        // Levenshtein / Token similarity
        double sim = calculateLevenshteinSimilarity(s1, s2);
        if (sim >= 0.80) {
            return BigDecimal.valueOf(sim * 40).setScale(2, RoundingMode.HALF_UP);
        } else if (sim >= 0.60) {
            return BigDecimal.valueOf(sim * 30).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateDateScore(java.time.LocalDate d1, java.time.LocalDate d2) {
        if (d1 == null || d2 == null) return BigDecimal.ZERO;
        long daysBetween = Math.abs(ChronoUnit.DAYS.between(d1, d2));

        if (daysBetween == 0) {
            return new BigDecimal("20.00");
        } else if (daysBetween <= 7) {
            return new BigDecimal("15.00");
        } else if (daysBetween <= 30) {
            return new BigDecimal("10.00");
        } else if (daysBetween <= 60) {
            return new BigDecimal("5.00");
        }
        return BigDecimal.ZERO;
    }

    private double calculateLevenshteinSimilarity(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        int maxLen = Math.max(len1, len2);
        if (maxLen == 0) return 1.0;
        return 1.0 - ((double) dp[len1][len2] / maxLen);
    }

    @Transactional(readOnly = true)
    public List<DuplicateAlertResponse> getAllAlerts() {
        return duplicateAlertRepository.findAllByOrderByDetectedAtDesc().stream()
                .map(DuplicateAlertResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public DuplicateAlertResponse reviewAlert(Long alertId, DuplicateStatus decision) {
        DuplicateAlert alert = duplicateAlertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException("Duplicate alert not found with ID: " + alertId));
        alert.setStatus(decision);
        duplicateAlertRepository.save(alert);
        return new DuplicateAlertResponse(alert);
    }
}
