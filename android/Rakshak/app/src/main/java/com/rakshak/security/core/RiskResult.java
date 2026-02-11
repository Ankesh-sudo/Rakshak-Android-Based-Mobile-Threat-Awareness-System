package com.rakshak.security.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the result of risk analysis for calls, SMS, files, links, etc.
 * This class is immutable after creation.
 */
public class RiskResult {

    // Final risk score (0–100)
    public final int score;

    // Risk level derived from score
    public final RiskLevel level;

    // Human-readable reasons shown to user
    private final List<String> reasons;

    public RiskResult(int score, RiskLevel level, List<String> reasons) {
        this.score = score;
        this.level = level;
        this.reasons = reasons != null
                ? new ArrayList<>(reasons)
                : new ArrayList<>();
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
    // RISK CATEGORIES
    // =================================================
    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH
    }
}
