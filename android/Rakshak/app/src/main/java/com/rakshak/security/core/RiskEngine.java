package com.rakshak.security.core;

import android.content.Context;
import android.text.TextUtils;

import com.rakshak.security.calls.engine.CallRiskResult;
import com.rakshak.security.calls.engine.CallThreatEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RiskEngine {

    // =================================================
    // SMS RISK CONFIG
    // =================================================
    private static final int SMS_URGENT_KEYWORD_RISK = 20;
    private static final int SMS_SENSITIVE_INFO_RISK = 30;
    private static final int SMS_SUSPICIOUS_LINK_RISK = 35;
    private static final int SMS_UNKNOWN_SENDER_RISK = 15;
    private static final int SMS_INTERNATIONAL_RISK = 20;
    private static final int SMS_SHORT_MESSAGE_RISK = 10;
    private static final int SMS_REPEATED_PATTERN_RISK = 10;
    private static final int TRUSTED_SENDER_REDUCTION = 15;

    private static final Pattern SUSPICIOUS_TLD_PATTERN =
            Pattern.compile(".*\\.(ru|xyz|click|top|gq|tk)(/.*)?");

    private static final Pattern URL_PATTERN =
            Pattern.compile("(http|https)://[^\\s]+");

    // =================================================
    // CALL ANALYSIS
    // =================================================
    public static RiskResult analyzeIncomingCall(
            Context context,
            String phoneNumber
    ) {

        if (TextUtils.isEmpty(phoneNumber)) {
            return new RiskResult(
                    40,
                    RiskResult.RiskLevel.MEDIUM,
                    List.of("Hidden or unavailable number detected")
            );
        }

        CallRiskResult callResult =
                CallThreatEngine.evaluate(context, phoneNumber);

        int score = clamp(callResult.getRiskScore());

        List<String> reasons = new ArrayList<>();
        reasons.add("Evaluated by Intelligent Call Threat Engine");
        reasons.add("Call Risk Score: " + score);

        return buildResult(score, reasons);
    }

    // =================================================
    // SMS ANALYSIS
    // =================================================
    public static RiskResult analyzeIncomingSms(
            String sender,
            String message
    ) {

        int score = 0;
        List<String> reasons = new ArrayList<>();

        String text = message != null ? message.toLowerCase() : "";

        if (TextUtils.isEmpty(sender)) {
            score += SMS_UNKNOWN_SENDER_RISK;
            reasons.add("Unknown or hidden sender detected");
        }

        if (!TextUtils.isEmpty(sender) && sender.startsWith("+")) {
            score += SMS_INTERNATIONAL_RISK;
            reasons.add("International number detected");
        }

        if (containsAny(text,
                "urgent", "immediately", "blocked", "suspended",
                "final notice", "legal action", "act now")) {
            score += SMS_URGENT_KEYWORD_RISK;
            reasons.add("Urgent or threatening language detected");
        }

        if (containsAny(text,
                "otp", "kyc", "bank", "verify", "account",
                "password", "upi", "pin", "credit card")) {
            score += SMS_SENSITIVE_INFO_RISK;
            reasons.add("Sensitive information request detected");
        }

        if (URL_PATTERN.matcher(text).find()) {

            score += SMS_SUSPICIOUS_LINK_RISK;
            reasons.add("Clickable URL detected in SMS");

            if (SUSPICIOUS_TLD_PATTERN.matcher(text).find()) {
                score += 10;
                reasons.add("High-risk domain extension detected");
            }
        }

        if (!TextUtils.isEmpty(text) && text.length() < 20) {
            score += SMS_SHORT_MESSAGE_RISK;
            reasons.add("Very short message detected");
        }

        if (text.matches(".*([!$*])\\1{2,}.*")) {
            score += SMS_REPEATED_PATTERN_RISK;
            reasons.add("Excessive special character pattern detected");
        }

        if (!TextUtils.isEmpty(sender)
                && sender.matches("^[A-Z]{2}-[A-Z0-9]{2,6}$")) {

            score -= TRUSTED_SENDER_REDUCTION;
            reasons.add("Registered sender ID format detected");
        }

        score = clamp(score);

        return buildResult(score, reasons);
    }

    // =================================================
    // OVERALL DEVICE RISK SCORING (UPDATED)
    // =================================================
    public static SecurityRiskResult evaluateOverallRisk(
            Context context,
            String recentCallNumber
    ) {

        SecurityRiskModel model = new SecurityRiskModel();

        // Call Risk
        if (!TextUtils.isEmpty(recentCallNumber)) {
            CallRiskResult callResult =
                    CallThreatEngine.evaluate(context, recentCallNumber);
            model.callRisk = clamp(callResult.getRiskScore());
        }

        // SMS Risk (🔥 Updated from storage)
        model.smsRisk =
                SecurityRiskStorage.getLastSmsRisk(context);

        // Future integrations
        model.fileRisk = 0;
        model.linkRisk = 0;
        model.permissionRisk = 0;
        model.healthRisk = 0;

        int totalScore = model.getTotalRiskScore();

        SecurityRiskResult.OverallThreatLevel level;

        if (totalScore >= 200)
            level = SecurityRiskResult.OverallThreatLevel.CRITICAL;
        else if (totalScore >= 150)
            level = SecurityRiskResult.OverallThreatLevel.HIGH;
        else if (totalScore >= 100)
            level = SecurityRiskResult.OverallThreatLevel.MODERATE;
        else if (totalScore >= 50)
            level = SecurityRiskResult.OverallThreatLevel.LOW;
        else
            level = SecurityRiskResult.OverallThreatLevel.SAFE;

        return new SecurityRiskResult(level, totalScore);
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
        return Math.max(0, Math.min(score, 100));
    }

    private static RiskResult buildResult(int score, List<String> reasons) {

        RiskResult.RiskLevel level;

        if (score >= 60)
            level = RiskResult.RiskLevel.HIGH;
        else if (score >= 30)
            level = RiskResult.RiskLevel.MEDIUM;
        else
            level = RiskResult.RiskLevel.LOW;

        return new RiskResult(score, level, reasons);
    }
}
