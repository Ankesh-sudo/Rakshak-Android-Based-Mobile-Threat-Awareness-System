package com.rakshak.security.core;

/**
 * Represents overall device security risk summary.
 * Immutable and safe for UI consumption.
 */
public final class SecurityRiskResult {

    public enum OverallThreatLevel {
        SAFE,
        LOW,
        MODERATE,
        HIGH,
        CRITICAL
    }

    private final OverallThreatLevel level;
    private final int totalScore;

    public SecurityRiskResult(OverallThreatLevel level, int totalScore) {
        this.level = level != null ? level : OverallThreatLevel.SAFE;
        this.totalScore = clamp(totalScore);
    }

    // =================================================
    // GETTERS
    // =================================================

    /**
     * Preferred getter (clean naming).
     */
    public OverallThreatLevel getLevel() {
        return level;
    }

    /**
     * Backward compatibility for dashboard.
     * Prevents "cannot find symbol getThreatLevel()" error.
     */
    public OverallThreatLevel getThreatLevel() {
        return level;
    }

    public int getTotalScore() {
        return totalScore;
    }

    // =================================================
    // HELPERS
    // =================================================

    public boolean isSafe() {
        return level == OverallThreatLevel.SAFE;
    }

    public boolean isHighOrCritical() {
        return level == OverallThreatLevel.HIGH
                || level == OverallThreatLevel.CRITICAL;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(value, 300));
    }

    @Override
    public String toString() {
        return "SecurityRiskResult{" +
                "level=" + level +
                ", totalScore=" + totalScore +
                '}';
    }
}
