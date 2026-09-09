package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.dto.ReceiptParseResponse;
import com.vsp.expenseclaims.entity.enums.ExpenseCategory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReceiptParsingService {

    // Amount patterns: Rs. 450, 450 INR, ₹1,850.50, 450/-
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?i)(?:inr|rs\\.?|₹)\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)|([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)\\s*(?:inr|rs\\.?|/-)");

    // Date patterns
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");
    private static final Pattern SLASH_DATE_PATTERN = Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})\\b");
    private static final Pattern TEXT_DATE_PATTERN = Pattern.compile(
            "(?i)\\b(\\d{1,2})\\s+(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)\\s+(\\d{2,4})\\b");

    public ReceiptParseResponse parse(String receiptText) {
        if (receiptText == null || receiptText.trim().isEmpty()) {
            return new ReceiptParseResponse("Unknown Merchant", BigDecimal.ZERO, LocalDate.now(), ExpenseCategory.OTHER, 0, "");
        }

        String text = receiptText.trim();
        int confidence = 30;

        // 1. Extract Amount
        BigDecimal amount = extractAmount(text);
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            confidence += 30;
        } else {
            amount = BigDecimal.ZERO;
        }

        // 2. Extract Date
        LocalDate date = extractDate(text);
        if (date != null) {
            confidence += 20;
        } else {
            date = LocalDate.now();
        }

        // 3. Extract Merchant & Category
        MerchantCategoryResult mc = extractMerchantAndCategory(text);
        if (mc.merchantFound) {
            confidence += 15;
        }

        return new ReceiptParseResponse(
                mc.merchant,
                amount,
                date,
                mc.category,
                Math.min(confidence, 98),
                text
        );
    }

    private BigDecimal extractAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        if (matcher.find()) {
            String val = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (val != null) {
                try {
                    String clean = val.replace(",", "").trim();
                    return new BigDecimal(clean);
                } catch (Exception ignored) {}
            }
        }

        // Fallback: look for any isolated number with currency context
        Pattern numberPattern = Pattern.compile("\\b([0-9]+(?:\\.[0-9]{1,2})?)\\b");
        Matcher numMatcher = numberPattern.matcher(text);
        BigDecimal lastNum = null;
        while (numMatcher.find()) {
            try {
                BigDecimal candidate = new BigDecimal(numMatcher.group(1));
                // Avoid picking years like 2026 as amounts if larger candidate exists
                if (candidate.compareTo(new BigDecimal("1900")) >= 0 && candidate.compareTo(new BigDecimal("2100")) <= 0) {
                    continue;
                }
                lastNum = candidate;
            } catch (Exception ignored) {}
        }
        return lastNum != null ? lastNum : BigDecimal.ZERO;
    }

    private LocalDate extractDate(String text) {
        // ISO YYYY-MM-DD
        Matcher iso = ISO_DATE_PATTERN.matcher(text);
        if (iso.find()) {
            try {
                return LocalDate.parse(iso.group(1), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) {}
        }

        // Textual: 14 Aug 2026 or 14 August 2026
        Matcher txt = TEXT_DATE_PATTERN.matcher(text);
        if (txt.find()) {
            try {
                int day = Integer.parseInt(txt.group(1));
                String monthStr = txt.group(2).substring(0, 3).toUpperCase(Locale.ROOT);
                int year = Integer.parseInt(txt.group(3));
                if (year < 100) year += 2000;
                Month month = Month.valueOf(switch (monthStr) {
                    case "JAN" -> "JANUARY";
                    case "FEB" -> "FEBRUARY";
                    case "MAR" -> "MARCH";
                    case "APR" -> "APRIL";
                    case "MAY" -> "MAY";
                    case "JUN" -> "JUNE";
                    case "JUL" -> "JULY";
                    case "AUG" -> "AUGUST";
                    case "SEP" -> "SEPTEMBER";
                    case "OCT" -> "OCTOBER";
                    case "NOV" -> "NOVEMBER";
                    case "DEC" -> "DECEMBER";
                    default -> "JANUARY";
                });
                return LocalDate.of(year, month, day);
            } catch (Exception ignored) {}
        }

        // Slash/dash: DD/MM/YYYY or DD-MM-YYYY
        Matcher slash = SLASH_DATE_PATTERN.matcher(text);
        if (slash.find()) {
            try {
                int day = Integer.parseInt(slash.group(1));
                int month = Integer.parseInt(slash.group(2));
                int year = Integer.parseInt(slash.group(3));
                if (year < 100) year += 2000;
                return LocalDate.of(year, month, day);
            } catch (Exception ignored) {}
        }

        return LocalDate.now();
    }

    private MerchantCategoryResult extractMerchantAndCategory(String text) {
        String lower = text.toLowerCase(Locale.ROOT);

        // Taxi / Ride
        if (lower.contains("uber")) {
            return new MerchantCategoryResult("Uber", ExpenseCategory.TAXI, true);
        }
        if (lower.contains("ola")) {
            return new MerchantCategoryResult("Ola", ExpenseCategory.TAXI, true);
        }
        if (lower.contains("rapido")) {
            return new MerchantCategoryResult("Rapido", ExpenseCategory.TAXI, true);
        }
        if (lower.contains("auto") || lower.contains("taxi") || lower.contains("cab")) {
            return new MerchantCategoryResult("City Taxi / Auto", ExpenseCategory.TAXI, true);
        }

        // Meals / Dining
        if (lower.contains("swiggy")) {
            return new MerchantCategoryResult("Swiggy", ExpenseCategory.MEALS, true);
        }
        if (lower.contains("zomato")) {
            return new MerchantCategoryResult("Zomato", ExpenseCategory.MEALS, true);
        }
        if (lower.contains("starbucks")) {
            return new MerchantCategoryResult("Starbucks", ExpenseCategory.MEALS, true);
        }
        if (lower.contains("chai point")) {
            return new MerchantCategoryResult("Chai Point", ExpenseCategory.MEALS, true);
        }
        if (lower.contains("restaurant") || lower.contains("cafe") || lower.contains("dinner") || lower.contains("lunch")) {
            return new MerchantCategoryResult("Restaurant / Dining", ExpenseCategory.MEALS, true);
        }

        // Travel / Flights / Stays
        if (lower.contains("indigo")) {
            return new MerchantCategoryResult("IndiGo Airlines", ExpenseCategory.TRAVEL, true);
        }
        if (lower.contains("air india")) {
            return new MerchantCategoryResult("Air India", ExpenseCategory.TRAVEL, true);
        }
        if (lower.contains("makemytrip")) {
            return new MerchantCategoryResult("MakeMyTrip", ExpenseCategory.TRAVEL, true);
        }
        if (lower.contains("hotel") || lower.contains("flight") || lower.contains("irctc") || lower.contains("train")) {
            return new MerchantCategoryResult("Travel Booking", ExpenseCategory.TRAVEL, true);
        }

        // Supplies
        if (lower.contains("croma")) {
            return new MerchantCategoryResult("Croma Electronics", ExpenseCategory.SUPPLIES, true);
        }
        if (lower.contains("amazon")) {
            return new MerchantCategoryResult("Amazon Supplies", ExpenseCategory.SUPPLIES, true);
        }
        if (lower.contains("stationery") || lower.contains("supplies") || lower.contains("print")) {
            return new MerchantCategoryResult("Office Supplies", ExpenseCategory.SUPPLIES, true);
        }

        // Generic fallback: pick first 2-3 words as merchant
        String[] words = text.split("\\s+");
        String candidateMerchant = words.length > 0 ? words[0] : "General Vendor";
        if (words.length > 1 && !words[1].matches("(?i)\\d+|inr|rs\\.?|₹")) {
            candidateMerchant += " " + words[1];
        }

        return new MerchantCategoryResult(candidateMerchant, ExpenseCategory.OTHER, false);
    }

    private record MerchantCategoryResult(String merchant, ExpenseCategory category, boolean merchantFound) {}
}
