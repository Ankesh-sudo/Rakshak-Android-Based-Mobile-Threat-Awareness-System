package com.rakshak.security.core;

public class SecurityRiskResult {

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
        this.level = level;
        this.totalScore = totalScore;
    }

    public OverallThreatLevel getLevel() {
        return level;
    }

    public int getTotalScore() {
        return totalScore;
    }
}
