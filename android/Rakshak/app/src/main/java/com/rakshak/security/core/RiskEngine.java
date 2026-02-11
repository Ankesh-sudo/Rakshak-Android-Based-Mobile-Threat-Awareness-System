package com.rakshak.security.core;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class RiskEngine {

    // =================================================
    // CALL RISK CONFIG (STEP 2.1)
    // =================================================
    private static final int HIDDEN_NUMBER_RISK = 30;
    private static final int NOT_IN_CONTACTS_RISK = 20;
    private static final int SHORT_NUMBER_RISK = 15;
    private static final int INTERNATIONAL_PATTERN_RISK = 15;

    private static final int SAVED_CONTACT_TRUST_REDUCTION = 25;
    private static final int FREQUENT_CALL_TRUST_REDUCTION = 15;

    private static final int SHORT_CALL_SCAM_RISK = 20;

    // =================================================
    // SMS RISK CONFIG (STEP 2.2)
    // =================================================
    private static final int SMS_URGENT_KEYWORD_RISK = 25;
    private static final int SMS_OTP_KYC_RISK = 30;
    private static final int SMS_SUSPICIOUS_LINK_RISK = 35;
    private static final int SMS_UNKNOWN_SENDER_RISK = 20;
    private static final int SMS_SHORT_MESSAGE_RISK = 10;

    private static final int TRUSTED_SENDER_REDUCTION = 20;

    // =================================================
    // CALL ANALYSIS (STABLE)
    // =================================================
    public static RiskResult analyzeIncomingCall(
            String phoneNumber,
            boolean isInContacts,
            int callFrequency,
            long lastCallDuration
    ) {

        int score = 0;
        List<String> reasons = new ArrayList<>();

        // Hidden / unknown number
        if (TextUtils.isEmpty(phoneNumber)) {
            score += HIDDEN_NUMBER_RISK;
            reasons.add("Caller number is hidden or unavailable");
        }

        // Not in contacts
        if (!isInContacts) {
            score += NOT_IN_CONTACTS_RISK;
            reasons.add("Caller is not in your contacts");
        }

        // Suspicious number patterns
        if (!TextUtils.isEmpty(phoneNumber)) {

            if (phoneNumber.length() < 10) {
                score += SHORT_NUMBER_RISK;
                reasons.add("Unusual phone number length detected");
            }

            if (phoneNumber.startsWith("00")) {
                score += INTERNATIONAL_PATTERN_RISK;
                reasons.add("International number pattern detected");
            }
        }

        // Trust reductions
        if (isInContacts) {
            score -= SAVED_CONTACT_TRUST_REDUCTION;
            reasons.add("Saved contact detected — reducing risk");
        }

        if (callFrequency >= 3) {
            score -= FREQUENT_CALL_TRUST_REDUCTION;
            reasons.add("Frequent caller pattern detected — reducing risk");
        }

        // Scam behavior
        if (lastCallDuration > 0 && lastCallDuration < 5) {
            score += SHORT_CALL_SCAM_RISK;
            reasons.add("Very short previous call detected — common scam behavior");
        }

        score = clamp(score);
        return buildResult(score, reasons);
    }

    // =================================================
    // SMS ANALYSIS (STEP 2.2 – IMPROVED)
    // =================================================
    public static RiskResult analyzeIncomingSms(
            String sender,
            String message
    ) {

        int score = 0;
        List<String> reasons = new ArrayList<>();

        String text = message != null ? message.toLowerCase() : "";

        // Unknown / empty sender
        if (TextUtils.isEmpty(sender)) {
            score += SMS_UNKNOWN_SENDER_RISK;
            reasons.add("Message sender is unknown or hidden");
        }

        // Urgency / threat language
        if (containsAny(text,
                "urgent", "immediately", "blocked", "suspended",
                "action required", "final notice")) {
            score += SMS_URGENT_KEYWORD_RISK;
            reasons.add("Urgent or threatening language detected");
        }

        // OTP / KYC / banking bait
        if (containsAny(text,
                "otp", "kyc", "bank", "verify", "account",
                "password", "upi", "pin")) {
            score += SMS_OTP_KYC_RISK;
            reasons.add("Sensitive information request detected");
        }

        // Suspicious links
        if (containsAny(text,
                "http://", "https://", "bit.ly", "tinyurl",
                ".ru", ".xyz", ".click")) {
            score += SMS_SUSPICIOUS_LINK_RISK;
            reasons.add("Suspicious or shortened link detected");
        }

        // Very short alarming messages
        if (text.length() > 0 && text.length() < 25) {
            score += SMS_SHORT_MESSAGE_RISK;
            reasons.add("Short alarming message detected");
        }

        // Trusted sender pattern (BANK-SMS, VM-*, etc.)
        if (!TextUtils.isEmpty(sender)
                && (sender.startsWith("VM-")
                || sender.startsWith("AD-")
                || sender.length() <= 6)) {
            score -= TRUSTED_SENDER_REDUCTION;
            reasons.add("Registered sender ID pattern detected — reducing risk");
        }

        score = clamp(score);
        return buildResult(score, reasons);
    }

    // =================================================
    // HELPERS
    // =================================================
    private static boolean containsAny(String text, String... keywords) {
        if (TextUtils.isEmpty(text)) return false;
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private static int clamp(int score) {
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
    }

    private static RiskResult buildResult(int score, List<String> reasons) {

        RiskResult.RiskLevel level;
        if (score >= 60) {
            level = RiskResult.RiskLevel.HIGH;
        } else if (score >= 30) {
            level = RiskResult.RiskLevel.MEDIUM;
        } else {
            level = RiskResult.RiskLevel.LOW;
        }

        return new RiskResult(score, level, reasons);
    }
}
