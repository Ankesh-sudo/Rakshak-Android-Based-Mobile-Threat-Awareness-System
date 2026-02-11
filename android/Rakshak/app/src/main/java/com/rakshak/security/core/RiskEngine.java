package com.rakshak.security.core;

import android.content.Context;
import android.text.TextUtils;

import com.rakshak.security.calls.engine.CallRiskResult;
import com.rakshak.security.calls.engine.CallThreatEngine;

import java.util.ArrayList;
import java.util.List;

public class RiskEngine {

    // =================================================
    // SMS RISK CONFIG
    // =================================================
    private static final int SMS_URGENT_KEYWORD_RISK = 25;
    private static final int SMS_OTP_KYC_RISK = 30;
    private static final int SMS_SUSPICIOUS_LINK_RISK = 35;
    private static final int SMS_UNKNOWN_SENDER_RISK = 20;
    private static final int SMS_SHORT_MESSAGE_RISK = 10;
    private static final int TRUSTED_SENDER_REDUCTION = 20;

    // =================================================
    // CALL ANALYSIS (Delegated to CallThreatEngine)
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
        reasons.add("Call threat evaluated by Intelligent Call Threat Engine");
        reasons.add("Call Risk Score: " + score);

        return buildResult(score, reasons);
    }

    // =================================================
    // SMS ANALYSIS (Advanced & Clean)
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
            reasons.add("Message sender is unknown or hidden");
        }

        if (containsAny(text,
                "urgent", "immediately", "blocked", "suspended",
                "action required", "final notice")) {
            score += SMS_URGENT_KEYWORD_RISK;
            reasons.add("Urgent or threatening language detected");
        }

        if (containsAny(text,
                "otp", "kyc", "bank", "verify", "account",
                "password", "upi", "pin")) {
            score += SMS_OTP_KYC_RISK;
            reasons.add("Sensitive information request detected");
        }

        if (containsAny(text,
                "http://", "https://", "bit.ly", "tinyurl",
                ".ru", ".xyz", ".click")) {
            score += SMS_SUSPICIOUS_LINK_RISK;
            reasons.add("Suspicious or shortened link detected");
        }

        if (text.length() > 0 && text.length() < 25) {
            score += SMS_SHORT_MESSAGE_RISK;
            reasons.add("Short alarming message detected");
        }

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
    // FUTURE OVERALL SECURITY SCORING
    // =================================================
    public static SecurityRiskResult evaluateOverallRisk(
            Context context,
            String recentCallNumber
    ) {

        SecurityRiskModel model = new SecurityRiskModel();

        if (!TextUtils.isEmpty(recentCallNumber)) {
            CallRiskResult callResult =
                    CallThreatEngine.evaluate(context, recentCallNumber);
            model.callRisk = callResult.getRiskScore();
        }

        // Future integrations:
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
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
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
