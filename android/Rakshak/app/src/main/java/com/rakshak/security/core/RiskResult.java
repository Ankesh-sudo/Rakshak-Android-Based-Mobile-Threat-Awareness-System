package com.rakshak.security.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the result of risk analysis for calls, SMS, files, links, etc.
 * Immutable and thread-safe after creation.
 */
public final class RiskResult {

    // Final risk score (0–100)
    private final int score;

    // Risk level derived from score
    private final RiskLevel level;

    // Human-readable reasons shown to user
    private final List<String> reasons;

    public RiskResult(int score, RiskLevel level, List<String> reasons) {
        this.score = clamp(score);
        this.level = level != null ? level : RiskLevel.LOW;
        this.reasons = reasons != null
                ? new ArrayList<>(reasons)
                : new ArrayList<>();
    }

    // =================================================
    // CORE GETTERS (🔥 Fixes your errors)
    // =================================================

    public int getScore() {
        return score;
    }

    public RiskLevel getRiskLevel() {
        return level;
    }

    // =================================================
    // RISK HELPERS
    // =================================================

    public boolean isHighRisk() {
        return level == RiskLevel.HIGH;
    }

    public boolean isMediumRisk() {
        return level == RiskLevel.MEDIUM;
    }

    public boolean isLowRisk() {
        return level == RiskLevel.LOW;
    }

    /**
     * True if the user should be warned.
     */
    public boolean shouldWarnUser() {
        return level == RiskLevel.MEDIUM || level == RiskLevel.HIGH;
    }

    // =================================================
    // UI & DISPLAY HELPERS
    // =================================================

    /**
     * Used by notification / overlay UI.
     */
    public String getExplanationText() {
        if (reasons.isEmpty()) {
            return "No specific risk indicators detected.";
        }

        StringBuilder builder = new StringBuilder();
        for (String reason : reasons) {
            builder.append("• ").append(reason).append("\n");
        }
        return builder.toString().trim();
    }

    /**
     * Returns reasons as read-only list (safe for UI).
     */
    public List<String> getReasons() {
        return Collections.unmodifiableList(reasons);
    }

    /**
     * Debug-friendly string.
     */
    @Override
    public String toString() {
        return "RiskResult{" +
                "score=" + score +
                ", level=" + level +
                ", reasons=" + reasons +
                '}';
    }

    // =================================================
    // INTERNAL SAFETY
    // =================================================

    private int clamp(int value) {
        return Math.max(0, Math.min(value, 100));
    }

    // =================================================
    // RISK CATEGORIES
    // =================================================

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH
        // You can add CRITICAL later if needed
    }
}
