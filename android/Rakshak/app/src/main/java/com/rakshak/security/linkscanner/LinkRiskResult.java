package com.rakshak.security.linkscanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the result of link risk analysis.
 * Explainable, immutable, UI & ML friendly.
 */
public class LinkRiskResult {

    // ===============================
    // RISK LEVEL
    // ===============================
    public enum RiskLevel {
        SAFE,
        SUSPICIOUS,
        DANGEROUS
    }

    // ===============================
    // CORE DATA
    // ===============================
    private final String originalUrl;
    private final String finalUrl;
    private final int riskScore;
    public final RiskLevel level;
    private final List<String> reasons;

    // ===============================
    // ADVANCED METADATA
    // ===============================
    private final double entropyScore;
    private final boolean shortened;
    private final boolean redirected;
    private final int confidence; // 0–100

    /**
     * Constructor used by LinkRiskEngine
     */
    public LinkRiskResult(
            String originalUrl,
            String finalUrl,
            int riskScore,
            RiskLevel level,
            List<String> reasons
    ) {
        this(
                originalUrl,
                finalUrl,
                riskScore,
                level,
                reasons,
                0.0,
                false,
                false
        );
    }

    /**
     * Advanced constructor (future-ready)
     */
    public LinkRiskResult(
            String originalUrl,
            String finalUrl,
            int riskScore,
            RiskLevel level,
            List<String> reasons,
            double entropyScore,
            boolean shortened,
            boolean redirected
    ) {
        this.originalUrl = originalUrl;
        this.finalUrl = finalUrl;
        this.riskScore = riskScore;
        this.level = level;
        this.reasons = reasons != null ? reasons : new ArrayList<>();
        this.entropyScore = entropyScore;
        this.shortened = shortened;
        this.redirected = redirected;
        this.confidence = calculateConfidence(riskScore, reasons.size());
    }

    // ===============================
    // GETTERS
    // ===============================

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public double getEntropyScore() {
        return entropyScore;
    }

    public boolean isShortened() {
        return shortened;
    }

    public boolean isRedirected() {
        return redirected;
    }

    public int getConfidence() {
        return confidence;
    }

    public List<String> getReasons() {
        return reasons;
    }

    // ===============================
    // UI HELPERS
    // ===============================

    public String getExplanationText() {
        if (reasons.isEmpty()) {
            return "No obvious risk indicators detected.";
        }

        StringBuilder sb = new StringBuilder();
        for (String r : reasons) {
            sb.append("• ").append(r).append("\n");
        }
        return sb.toString().trim();
    }

    public String getRiskLabel() {
        switch (level) {
            case DANGEROUS:
                return "High Risk";
            case SUSPICIOUS:
                return "Suspicious";
            default:
                return "Safe";
        }
    }

    /**
     * One-line summary for cards / notifications
     */
    public String getSummary() {
        switch (level) {
            case DANGEROUS:
                return "This link shows multiple strong scam indicators.";
            case SUSPICIOUS:
                return "This link has some suspicious patterns.";
            default:
                return "No major threats detected in this link.";
        }
    }

    /**
     * UI hint (no Android dependency)
     */
    public String getRiskColorHint() {
        switch (level) {
            case DANGEROUS:
                return "RED";
            case SUSPICIOUS:
                return "ORANGE";
            default:
                return "GREEN";
        }
    }

    // ===============================
    // INTERNAL LOGIC
    // ===============================

    private int calculateConfidence(int score, int signals) {
        int base = Math.min(100, score + (signals * 5));
        return Math.max(40, base);
    }
}
